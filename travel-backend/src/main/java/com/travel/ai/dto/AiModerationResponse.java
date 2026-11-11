package com.travel.ai.dto;

import java.util.List;

public record AiModerationResponse(
        String riskLevel,
        int score,
        List<String> categories,
        List<String> reasons,
        String suggestion,
        List<String> sensitiveWords,
        AiMeta meta) {

    public AiModerationResponse withMeta(AiMeta newMeta) {
        return new AiModerationResponse(riskLevel, score, categories, reasons,
                suggestion, sensitiveWords, newMeta);
    }
}
