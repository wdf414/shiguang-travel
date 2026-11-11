package com.travel.ai.dto;

import java.math.BigDecimal;

/**
 * AI 回答中引用的平台真实产品，前端可据此跳转到详情页。
 */
public record AiProductRef(
        String type,
        Long id,
        String name,
        String subtitle,
        BigDecimal price,
        String image,
        String url) {
}
