package com.travel.ai.dto;

/**
 * 每次 AI 调用的可观测信息，便于前端展示“是否命中缓存 / 是否降级 / 耗时”。
 */
public record AiMeta(
        String model,
        boolean cached,
        long latencyMs,
        int groundedProducts,
        boolean fallback,
        String generatedAt) {
}
