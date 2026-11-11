package com.travel.ai.dto;

import java.util.List;

public record AiChatResponse(
        String reply,
        List<AiProductRef> references,
        AiMeta meta) {

    public AiChatResponse withMeta(AiMeta newMeta) {
        return new AiChatResponse(reply, references, newMeta);
    }
}
