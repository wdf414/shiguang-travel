package com.travel.ai.dto;

/**
 * 只暴露是否配置，不返回任何密钥片段。
 */
public record AiHealthResponse(
        boolean enabled,
        boolean configured,
        boolean mockEnabled,
        String model,
        String baseUrl) {
}
