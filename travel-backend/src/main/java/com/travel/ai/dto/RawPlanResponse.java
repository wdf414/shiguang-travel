package com.travel.ai.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 大模型原始输出。ID 只允许来自提示词中的平台数据，服务层会再次校验。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawPlanResponse(
        String summary,
        List<RawDay> days,
        RawBudget budget,
        List<String> tips,
        List<Long> recommendedLineIds,
        List<Long> recommendedHotelIds) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawDay(
            Integer day,
            String title,
            String theme,
            List<RawItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawItem(
            String time,
            String title,
            String description,
            String type,
            Long refId,
            String tip) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RawBudget(
            BigDecimal transport,
            BigDecimal hotel,
            BigDecimal tickets,
            BigDecimal food,
            BigDecimal other,
            BigDecimal total,
            BigDecimal perPerson) {
    }
}
