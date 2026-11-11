package com.travel.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 服务的可配置项。
 *
 * <p>密钥只从环境变量 / .env 读取，不进入代码仓库。所有值在 application.yml 中提供默认值，
 * 因此本地开发只需配置 AI_API_KEY 即可。</p>
 */
@ConfigurationProperties(prefix = "ai")
public record AiProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        double temperature,
        int maxTokens,
        int timeoutSeconds,
        boolean mockEnabled,
        int cacheTtlSeconds,
        int rateLimitPerMinute) {

    public boolean configured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
