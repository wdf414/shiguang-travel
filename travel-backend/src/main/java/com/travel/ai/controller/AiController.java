package com.travel.ai.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.travel.ai.dto.AiChatRequest;
import com.travel.ai.dto.AiChatResponse;
import com.travel.ai.dto.AiHealthResponse;
import com.travel.ai.dto.AiModerationRequest;
import com.travel.ai.dto.AiModerationResponse;
import com.travel.ai.dto.AiPlanRequest;
import com.travel.ai.dto.AiPlanResponse;
import com.travel.ai.service.AiService;
import com.travel.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/plan")
    public Result<AiPlanResponse> plan(@Valid @RequestBody AiPlanRequest request,
                                       HttpServletRequest httpRequest) {
        return Result.success(aiService.plan(request, clientIp(httpRequest)));
    }

    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request,
                                       HttpServletRequest httpRequest) {
        return Result.success(aiService.chat(request, clientIp(httpRequest)));
    }

    @GetMapping("/health")
    public Result<AiHealthResponse> health() {
        return Result.success(aiService.health());
    }

    @PostMapping("/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<AiModerationResponse> moderate(@Valid @RequestBody AiModerationRequest request) {
        return Result.success(aiService.moderate(request));
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
