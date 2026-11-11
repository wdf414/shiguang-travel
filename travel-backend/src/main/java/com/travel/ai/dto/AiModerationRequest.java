package com.travel.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiModerationRequest(
        @NotBlank(message = "内容类型不能为空")
        @Pattern(regexp = "travel_note|companion|comment", message = "不支持的内容类型")
        String contentType,

        @Size(max = 200, message = "标题不能超过 200 字")
        String title,

        @NotBlank(message = "待审核内容不能为空")
        @Size(max = 5000, message = "待审核内容不能超过 5000 字")
        String content) {
}
