package com.travel.ai.dto;

import java.math.BigDecimal;
import java.util.List;

public record AiPlanResponse(
        String summary,
        int days,
        List<DayPlan> itinerary,
        Budget budget,
        List<AiProductRef> recommendedLines,
        List<AiProductRef> recommendedHotels,
        List<String> tips,
        AiMeta meta) {

    public AiPlanResponse withMeta(AiMeta newMeta) {
        return new AiPlanResponse(summary, days, itinerary, budget,
                recommendedLines, recommendedHotels, tips, newMeta);
    }

    public record DayPlan(
            int day,
            String title,
            String theme,
            List<PlanItem> items) {
    }

    public record PlanItem(
            String time,
            String title,
            String description,
            String type,
            Long refId,
            String refName,
            String tip) {
    }

    public record Budget(
            BigDecimal transport,
            BigDecimal hotel,
            BigDecimal tickets,
            BigDecimal food,
            BigDecimal other,
            BigDecimal total,
            BigDecimal perPerson,
            String currency) {
    }
}
