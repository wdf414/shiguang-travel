package com.travel.ai.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AiChatMessage(
        @Pattern(regexp = "user|assistant", message = "消息角色只能是 user 或 assistant")
        String role,

        @Size(max = 1000, message = "单条历史消息不能超过 1000 字")
        String content) {
}
