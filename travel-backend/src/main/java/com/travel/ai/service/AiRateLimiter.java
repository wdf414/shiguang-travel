package com.travel.ai.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.travel.ai.config.AiProperties;
import com.travel.exception.BusinessException;

/**
 * 按 IP + 分钟窗口限流，防止演示账号被刷、保护 API 费用。
 * Redis 可用时多实例共享计数，不可用时退化为单机计数。
 */
@Service
public class AiRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(AiRateLimiter.class);
    private static final String PREFIX = "shiguang:ai:rate:";

    private final StringRedisTemplate redisTemplate;
    private final AiProperties properties;
    private final Map<String, AtomicInteger> localCounters = new ConcurrentHashMap<>();

    public AiRateLimiter(StringRedisTemplate redisTemplate, AiProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public void check(String ip) {
        int limit = properties.rateLimitPerMinute();
        if (limit <= 0) {
            return;
        }
        String safeIp = ip == null || ip.isBlank() ? "unknown" : ip;
        String minute = String.valueOf(System.currentTimeMillis() / 60_000);
        String key = PREFIX + safeIp + ":" + minute;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofMinutes(2));
            }
            if (count != null && count > limit) {
                throw tooMany();
            }
            return;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Redis rate limiter unavailable, using in-memory counter: {}", e.getMessage());
        }

        // 本地兜底：只保留当前分钟附近的计数，避免内存无限增长。
        if (localCounters.size() > 10_000) {
            localCounters.clear();
        }
        int count = localCounters.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet();
        if (count > limit) {
            throw tooMany();
        }
    }

    private BusinessException tooMany() {
        return new BusinessException(429, "AI 请求过于频繁，请稍后再试");
    }
}
