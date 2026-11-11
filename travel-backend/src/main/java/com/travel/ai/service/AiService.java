package com.travel.ai.service;

import com.travel.ai.dto.AiChatRequest;
import com.travel.ai.dto.AiChatResponse;
import com.travel.ai.dto.AiHealthResponse;
import com.travel.ai.dto.AiModerationRequest;
import com.travel.ai.dto.AiModerationResponse;
import com.travel.ai.dto.AiPlanRequest;
import com.travel.ai.dto.AiPlanResponse;

public interface AiService {

    AiPlanResponse plan(AiPlanRequest request, String clientIp);

    AiChatResponse chat(AiChatRequest request, String clientIp);

    AiModerationResponse moderate(AiModerationRequest request);

    AiHealthResponse health();
}
