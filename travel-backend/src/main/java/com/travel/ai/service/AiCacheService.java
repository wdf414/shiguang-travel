package com.travel.ai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 两级缓存：Redis（多实例共享）+ 进程内 LRU 兜底（Redis 不可用时仍可服务）。
 * 相同请求在 TTL 内直接复用结果，降低大模型调用成本和首字节延迟。
 */
@Service
public class AiCacheService {
    private static final Logger log = LoggerFactory.getLogger(AiCacheService.class);
    private static final String PREFIX = "shiguang:ai:cache:";
    private static final int LOCAL_MAX_ENTRIES = 500;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalEntry> localCache = new ConcurrentHashMap<>();

    public AiCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> T get(String key, Class<T> type) {
        String cacheKey = PREFIX + key;
        String json = null;
        try {
            json = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.debug("Redis cache unavailable, falling back to local cache: {}", e.getMessage());
        }
        if (json == null) {
            LocalEntry entry = localCache.get(cacheKey);
            if (entry != null && entry.expiresAt() > System.currentTimeMillis()) {
                json = entry.json();
            } else if (entry != null) {
                localCache.remove(cacheKey);
            }
        }
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Failed to deserialize AI cache entry, dropping it: {}", e.getMessage());
            localCache.remove(cacheKey);
            return null;
        }
    }

    public void put(String key, Object value, int ttlSeconds) {
        if (value == null || ttlSeconds <= 0) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(value);
            String cacheKey = PREFIX + key;
            try {
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(ttlSeconds));
            } catch (Exception e) {
                log.debug("Redis cache write failed, using local cache only: {}", e.getMessage());
            }
            if (localCache.size() >= LOCAL_MAX_ENTRIES) {
                cleanupLocalCache();
            }
            localCache.put(cacheKey, new LocalEntry(json, System.currentTimeMillis() + ttlSeconds * 1000L));
        } catch (Exception e) {
            log.warn("Failed to serialize AI cache entry: {}", e.getMessage());
        }
    }

    public String hashKey(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private void cleanupLocalCache() {
        long now = System.currentTimeMillis();
        localCache.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        if (localCache.size() >= LOCAL_MAX_ENTRIES) {
            localCache.clear();
        }
    }

    private record LocalEntry(String json, long expiresAt) {
    }
}
