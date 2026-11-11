package com.travel.ai.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.config.AiProperties;
import com.travel.exception.BusinessException;

/**
 * OpenAI 兼容协议的轻量客户端。
 *
 * <p>没有引入厂商 SDK，而是直接调用 /chat/completions，这样 DeepSeek、通义千问、
 * 自建 vLLM 等只要兼容该协议就能切换，降低供应商锁定风险。</p>
 */
@Component
public class AiClient {
    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    private final RestClient restClient;
    private final AiProperties properties;
    private final ObjectMapper objectMapper;

    public AiClient(RestClient aiRestClient, AiProperties properties, ObjectMapper objectMapper) {
        this.restClient = aiRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public boolean configured() {
        return properties.configured();
    }

    public Completion chat(List<AiMessage> messages, boolean jsonMode) {
        if (!properties.enabled()) {
            throw new BusinessException(503, "AI 功能已关闭");
        }
        if (!configured()) {
            throw new BusinessException(503, "AI 服务未配置 API Key");
        }

        boolean useJsonMode = jsonMode;
        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            long started = System.currentTimeMillis();
            try {
                String requestJson = objectMapper.writeValueAsString(buildBody(messages, useJsonMode));
                String response = restClient.post()
                        .uri("/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + properties.apiKey())
                        // 显式长度可避免部分网关把请求改成 chunked 后兼容性变差。
                        .header("Content-Length", String.valueOf(requestJson.getBytes(StandardCharsets.UTF_8).length))
                        .body(requestJson)
                        .retrieve()
                        .body(String.class);

                JsonNode root = objectMapper.readTree(response);
                JsonNode choices = root.path("choices");
                if (!choices.isArray() || choices.isEmpty()) {
                    throw new BusinessException(502, "AI 返回结果为空");
                }
                String content = choices.get(0).path("message").path("content").asText("");
                if (content.isBlank()) {
                    throw new BusinessException(502, "AI 返回内容为空");
                }
                JsonNode usage = root.path("usage");
                long elapsed = System.currentTimeMillis() - started;
                log.info("AI call ok: model={}, jsonMode={}, latency={}ms, promptTokens={}, completionTokens={}",
                        properties.model(), useJsonMode, elapsed,
                        usage.path("prompt_tokens").asInt(0), usage.path("completion_tokens").asInt(0));
                return new Completion(content, elapsed,
                        usage.path("prompt_tokens").asInt(0),
                        usage.path("completion_tokens").asInt(0));
            } catch (RestClientResponseException e) {
                int status = e.getStatusCode().value();
                // 部分兼容服务不支持 response_format，自动降级为普通文本再重试一次。
                if (status == 400 && useJsonMode) {
                    log.warn("AI provider rejected json mode, retrying without response_format");
                    useJsonMode = false;
                    continue;
                }
                if (status == 429 || status >= 500) {
                    lastError = e;
                    sleepBeforeRetry(attempt);
                    continue;
                }
                log.warn("AI call failed: status={}, body={}", status, safeBody(e.getResponseBodyAsString()));
                throw new BusinessException(502, "AI 服务调用失败（HTTP " + status + "）");
            } catch (ResourceAccessException e) {
                lastError = e;
                log.warn("AI call timeout/network error, attempt={}: {}", attempt, e.getMessage());
                sleepBeforeRetry(attempt);
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.warn("AI response parse failed", e);
                throw new BusinessException(502, "AI 返回格式异常");
            }
        }
        log.warn("AI call exhausted retries: {}", lastError == null ? "unknown" : lastError.getMessage());
        throw new BusinessException(503, "AI 服务暂时不可用，请稍后重试");
    }

    private Map<String, Object> buildBody(List<AiMessage> messages, boolean jsonMode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.model());
        List<Map<String, String>> payload = new ArrayList<>();
        for (AiMessage message : messages) {
            payload.add(Map.of("role", message.role(), "content", message.content()));
        }
        body.put("messages", payload);
        body.put("temperature", properties.temperature());
        body.put("max_tokens", properties.maxTokens());
        body.put("stream", false);
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }
        return body;
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(300L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String safeBody(String body) {
        if (body == null) {
            return "";
        }
        return body.length() > 500 ? body.substring(0, 500) : body;
    }

    public record Completion(String content, long latencyMs, int promptTokens, int completionTokens) {
    }
}
