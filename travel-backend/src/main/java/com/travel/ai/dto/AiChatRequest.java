package com.travel.ai.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank(message = "问题不能为空")
        @Size(max = 1000, message = "问题不能超过 1000 字")
        String message,

        @Valid
        @Size(max = 8, message = "最多携带 8 条历史消息")
        List<AiChatMessage> history,

        @Size(max = 64, message = "会话标识过长")
        String sessionId) {
}
