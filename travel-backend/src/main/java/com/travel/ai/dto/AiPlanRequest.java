package com.travel.ai.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiPlanRequest(
        @NotBlank(message = "目的地不能为空")
        @Size(max = 50, message = "目的地名称过长")
        String destination,

        @Min(value = 1, message = "行程天数至少为 1 天")
        @Max(value = 10, message = "行程天数最多为 10 天")
        Integer days,

        @DecimalMin(value = "0", message = "预算不能为负数")
        BigDecimal budget,

        @Size(max = 50, message = "出发城市名称过长")
        String departureCity,

        @Size(max = 30, message = "出行风格名称过长")
        String travelStyle,

        @Size(max = 8, message = "偏好最多选择 8 项")
        List<@Size(max = 20, message = "偏好项过长") String> preferences,

        @Min(value = 1, message = "出行人数至少为 1 人")
        @Max(value = 20, message = "出行人数最多为 20 人")
        Integer companions) {

    public int normalizedDays() {
        return days == null ? 3 : days;
    }

    public int normalizedCompanions() {
        return companions == null ? 1 : companions;
    }
}
