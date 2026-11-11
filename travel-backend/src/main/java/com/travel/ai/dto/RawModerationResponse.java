package com.travel.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawModerationResponse(
        String riskLevel,
        Integer score,
        List<String> categories,
        List<String> reasons,
        String suggestion) {
}
