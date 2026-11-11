package com.travel.ai.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.ai.client.AiClient;
import com.travel.ai.client.AiMessage;
import com.travel.ai.config.AiProperties;
import com.travel.ai.dto.AiChatRequest;
import com.travel.ai.dto.AiChatResponse;
import com.travel.ai.dto.AiHealthResponse;
import com.travel.ai.dto.AiMeta;
import com.travel.ai.dto.AiModerationRequest;
import com.travel.ai.dto.AiModerationResponse;
import com.travel.ai.dto.AiPlanRequest;
import com.travel.ai.dto.AiPlanResponse;
import com.travel.ai.dto.AiProductRef;
import com.travel.ai.dto.RawModerationResponse;
import com.travel.ai.dto.RawPlanResponse;
import com.travel.ai.service.AiCacheService;
import com.travel.ai.service.AiContextService;
import com.travel.ai.service.AiLocalFallback;
import com.travel.ai.service.AiPromptFactory;
import com.travel.ai.service.AiRateLimiter;
import com.travel.ai.service.AiService;
import com.travel.entity.Hotel;
import com.travel.entity.ScenicSpot;
import com.travel.entity.TourLine;
import com.travel.exception.BusinessException;
import com.travel.service.ContentReviewService;

/**
 * AI 能力编排层。
 *
 * <p>一次请求依次经过：限流 → 缓存 → 平台数据召回（RAG）→ 提示词 → 大模型 → JSON 校验 →
 * 真实产品回填 → 结果缓存。模型不可用时降级为本地结果，并在 meta.fallback 中明确标记。</p>
 */
@Service
public class AiServiceImpl implements AiService {
    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);
    private static final Set<String> PLAN_ITEM_TYPES = Set.of(
            "transport", "hotel", "scenic", "food", "free");

    private final AiClient aiClient;
    private final AiProperties properties;
    private final AiContextService contextService;
    private final AiPromptFactory promptFactory;
    private final AiCacheService cacheService;
    private final AiRateLimiter rateLimiter;
    private final AiLocalFallback localFallback;
    private final ContentReviewService contentReviewService;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(AiClient aiClient,
                         AiProperties properties,
                         AiContextService contextService,
                         AiPromptFactory promptFactory,
                         AiCacheService cacheService,
                         AiRateLimiter rateLimiter,
                         AiLocalFallback localFallback,
                         ContentReviewService contentReviewService,
                         ObjectMapper objectMapper) {
        this.aiClient = aiClient;
        this.properties = properties;
        this.contextService = contextService;
        this.promptFactory = promptFactory;
        this.cacheService = cacheService;
        this.rateLimiter = rateLimiter;
        this.localFallback = localFallback;
        this.contentReviewService = contentReviewService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiPlanResponse plan(AiPlanRequest request, String clientIp) {
        rateLimiter.check(clientIp);
        String cacheKey = planCacheKey(request);
        AiPlanResponse cached = cacheService.get(cacheKey, AiPlanResponse.class);
        if (cached != null) {
            return cached.withMeta(cachedMeta(cached.meta()));
        }

        AiContextService.PlanContext context;
        try {
            context = contextService.buildPlanContext(
                    request.destination(), request.normalizedDays(), request.budget(), request.departureCity());
        } catch (Exception e) {
            log.error("Failed to build plan context", e);
            throw new BusinessException(503, "平台数据暂时不可用，请稍后重试");
        }

        if (!aiClient.configured()) {
            if (!properties.mockEnabled()) {
                throw new BusinessException(503, "AI 服务未配置 API Key，请联系管理员在 .env 中设置 AI_API_KEY");
            }
            return localFallback.plan(request, context, properties.model(), 0);
        }

        long started = System.currentTimeMillis();
        try {
            AiClient.Completion completion = aiClient.chat(List.of(
                    AiMessage.system(promptFactory.planSystemPrompt()),
                    AiMessage.user(promptFactory.planUserPrompt(request, context))), true);
            RawPlanResponse raw = parseJson(completion.content(), RawPlanResponse.class);
            AiPlanResponse response = mapPlan(raw, request, context, completion.latencyMs());
            cacheService.put(cacheKey, response, properties.cacheTtlSeconds());
            return response;
        } catch (Exception e) {
            log.warn("AI plan failed, returning local fallback: {}", e.getMessage());
            return localFallback.plan(request, context, properties.model(), System.currentTimeMillis() - started);
        }
    }

    @Override
    public AiChatResponse chat(AiChatRequest request, String clientIp) {
        rateLimiter.check(clientIp);
        boolean cacheable = request.history() == null || request.history().isEmpty();
        String cacheKey = "chat:" + cacheService.hashKey(request.message());
        if (cacheable) {
            AiChatResponse cached = cacheService.get(cacheKey, AiChatResponse.class);
            if (cached != null) {
                return cached.withMeta(cachedMeta(cached.meta()));
            }
        }

        AiContextService.ChatContext context;
        try {
            context = contextService.buildChatContext(request.message());
        } catch (Exception e) {
            log.error("Failed to build chat context", e);
            throw new BusinessException(503, "平台数据暂时不可用，请稍后重试");
        }

        if (!aiClient.configured()) {
            if (!properties.mockEnabled()) {
                throw new BusinessException(503, "AI 服务未配置 API Key，请联系管理员在 .env 中设置 AI_API_KEY");
            }
            return localFallback.chat(request.message(), context, properties.model(), 0);
        }

        long started = System.currentTimeMillis();
        try {
            List<AiMessage> messages = new ArrayList<>();
            messages.add(AiMessage.system(promptFactory.chatSystemPrompt()));
            if (request.history() != null) {
                request.history().stream()
                        .filter(Objects::nonNull)
                        .filter(message -> message.content() != null && !message.content().isBlank())
                        .limit(6)
                        .forEach(message -> messages.add(new AiMessage(
                                "assistant".equals(message.role()) ? "assistant" : "user",
                                trim(message.content(), 1000))));
            }
            messages.add(AiMessage.user(promptFactory.chatUserPrompt(request, context)));

            AiClient.Completion completion = aiClient.chat(messages, false);
            List<AiProductRef> refs = context.references().stream().limit(8).toList();
            AiChatResponse response = new AiChatResponse(
                    trim(completion.content(), 6000), refs,
                    meta(completion.latencyMs(), context.references().size(), false));
            if (cacheable) {
                cacheService.put(cacheKey, response, properties.cacheTtlSeconds());
            }
            return response;
        } catch (Exception e) {
            log.warn("AI chat failed, returning local fallback: {}", e.getMessage());
            return localFallback.chat(request.message(), context, properties.model(),
                    System.currentTimeMillis() - started);
        }
    }

    @Override
    public AiModerationResponse moderate(AiModerationRequest request) {
        List<String> sensitiveWords = contentReviewService.checkSensitiveWords(
                (request.title() == null ? "" : request.title()) + "\n" + request.content());
        if (!aiClient.configured()) {
            return localFallback.moderation(sensitiveWords, properties.model(), 0);
        }

        long started = System.currentTimeMillis();
        try {
            AiClient.Completion completion = aiClient.chat(List.of(
                    AiMessage.system(promptFactory.moderationSystemPrompt()),
                    AiMessage.user(promptFactory.moderationUserPrompt(request, sensitiveWords))), true);
            RawModerationResponse raw = parseJson(completion.content(), RawModerationResponse.class);
            return mapModeration(raw, sensitiveWords, completion.latencyMs());
        } catch (Exception e) {
            log.warn("AI moderation failed, returning rule-based fallback: {}", e.getMessage());
            return localFallback.moderation(sensitiveWords, properties.model(),
                    System.currentTimeMillis() - started);
        }
    }

    @Override
    public AiHealthResponse health() {
        return new AiHealthResponse(properties.enabled(), aiClient.configured(),
                properties.mockEnabled(), properties.model(), properties.baseUrl());
    }

    private AiPlanResponse mapPlan(RawPlanResponse raw, AiPlanRequest request,
                                   AiContextService.PlanContext context, long latencyMs) {
        if (raw == null || raw.days() == null || raw.days().isEmpty()) {
            throw new BusinessException(502, "AI 未返回有效行程");
        }

        List<AiPlanResponse.DayPlan> days = new ArrayList<>();
        int dayCount = Math.min(Math.min(raw.days().size(), 10), request.normalizedDays());
        for (int i = 0; i < dayCount; i++) {
            RawPlanResponse.RawDay rawDay = raw.days().get(i);
            List<AiPlanResponse.PlanItem> items = new ArrayList<>();
            if (rawDay != null && rawDay.items() != null) {
                for (RawPlanResponse.RawItem rawItem : rawDay.items()) {
                    if (items.size() >= 8 || rawItem == null || isBlank(rawItem.title())) {
                        continue;
                    }
                    String type = normalizeType(rawItem.type());
                    Long refId = validRefId(type, rawItem.refId(), context);
                    items.add(new AiPlanResponse.PlanItem(
                            isBlank(rawItem.time()) ? "-" : trim(rawItem.time(), 20),
                            trim(rawItem.title(), 80),
                            trim(rawItem.description(), 200),
                            type,
                            refId,
                            resolveRefName(type, refId, context, rawItem.title()),
                            trim(rawItem.tip(), 120)));
                }
            }
            if (items.isEmpty()) {
                items.add(new AiPlanResponse.PlanItem("-", "自由活动", "当天以休整和自由探索为主。",
                        "free", null, null, null));
            }
            days.add(new AiPlanResponse.DayPlan(
                    i + 1,
                    isBlank(rawDay == null ? null : rawDay.title())
                            ? "第 " + (i + 1) + " 天"
                            : trim(rawDay.title(), 80),
                    isBlank(rawDay == null ? null : rawDay.theme()) ? "综合" : trim(rawDay.theme(), 30),
                    items));
        }

        // 模型少返回天数时补齐，避免前端时间线断裂。
        for (int day = days.size() + 1; day <= request.normalizedDays(); day++) {
            days.add(new AiPlanResponse.DayPlan(day, "第 " + day + " 天", "自由活动",
                    List.of(new AiPlanResponse.PlanItem("-", "自由活动",
                            "可参考推荐线路或景点自行安排。", "free", null, null, null))));
        }

        AiPlanResponse.Budget budget = mapBudget(raw.budget(), request, context);
        List<AiProductRef> lines = pickRefs(raw.recommendedLineIds(), context.lines(),
                TourLine::getId, contextService::toLineRef);
        List<AiProductRef> hotels = pickRefs(raw.recommendedHotelIds(), context.hotels(),
                Hotel::getId, contextService::toHotelRef);
        if (lines.isEmpty()) {
            lines = context.lines().stream().limit(3).map(contextService::toLineRef).toList();
        }
        if (hotels.isEmpty()) {
            hotels = context.hotels().stream().limit(3).map(contextService::toHotelRef).toList();
        }
        lines = contextService.dedupe(lines);
        hotels = contextService.dedupe(hotels);

        List<String> tips = raw.tips() == null ? List.of()
                : raw.tips().stream().filter(tip -> !isBlank(tip)).map(tip -> trim(tip, 120)).limit(5).toList();
        if (tips.isEmpty()) {
            tips = List.of("行程为 AI 参考方案，实际价格与库存以下单页为准。",
                    "旺季建议提前预订，并确认退改规则。");
        }

        int grounded = context.lines().size() + context.hotels().size() + context.spots().size();
        String summary = isBlank(raw.summary())
                ? "已根据平台真实产品生成 " + request.normalizedDays() + " 天行程。"
                : trim(raw.summary(), 200);
        return new AiPlanResponse(summary, request.normalizedDays(), days, budget, lines, hotels,
                tips, meta(latencyMs, grounded, false));
    }

    private AiPlanResponse.Budget mapBudget(RawPlanResponse.RawBudget raw, AiPlanRequest request,
                                            AiContextService.PlanContext context) {
        if (raw == null) {
            AiPlanResponse fallback = localFallback.plan(request, context, properties.model(), 0);
            return fallback.budget();
        }
        BigDecimal transport = nvl(raw.transport());
        BigDecimal hotel = nvl(raw.hotel());
        BigDecimal tickets = nvl(raw.tickets());
        BigDecimal food = nvl(raw.food());
        BigDecimal other = nvl(raw.other());
        BigDecimal total = raw.total() == null
                ? transport.add(hotel).add(tickets).add(food).add(other)
                : raw.total();
        int people = Math.max(1, request.normalizedCompanions());
        BigDecimal perPerson = raw.perPerson() == null
                ? total.divide(BigDecimal.valueOf(people), 0, RoundingMode.HALF_UP)
                : raw.perPerson();
        return new AiPlanResponse.Budget(transport, hotel, tickets, food, other,
                total.setScale(0, RoundingMode.HALF_UP),
                perPerson.setScale(0, RoundingMode.HALF_UP), "CNY");
    }

    private AiModerationResponse mapModeration(RawModerationResponse raw, List<String> sensitiveWords,
                                               long latencyMs) {
        if (raw == null) {
            throw new BusinessException(502, "AI 未返回有效审核结果");
        }
        int score = raw.score() == null ? 0 : Math.max(0, Math.min(100, raw.score()));
        String riskLevel = normalizeRisk(raw.riskLevel(), score);
        String suggestion = normalizeSuggestion(raw.suggestion(), riskLevel);

        LinkedHashSet<String> categories = new LinkedHashSet<>();
        if (raw.categories() != null) {
            raw.categories().stream().filter(c -> !isBlank(c)).map(c -> trim(c, 30)).limit(6)
                    .forEach(categories::add);
        }
        List<String> reasons = raw.reasons() == null ? new ArrayList<>()
                : raw.reasons().stream().filter(r -> !isBlank(r)).map(r -> trim(r, 100)).limit(5)
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        if (sensitiveWords != null && !sensitiveWords.isEmpty()) {
            categories.add("敏感词命中");
            reasons.add("命中平台敏感词：" + String.join("、", sensitiveWords));
            if ("LOW".equals(riskLevel)) {
                riskLevel = "MEDIUM";
            }
            if ("APPROVE".equals(suggestion)) {
                suggestion = "REVIEW";
            }
        }
        if (categories.isEmpty()) {
            categories.add("未发现明显风险");
        }
        if (reasons.isEmpty()) {
            reasons.add("未发现明显违规内容");
        }

        return new AiModerationResponse(riskLevel, score, List.copyOf(categories), reasons,
                suggestion, sensitiveWords == null ? List.of() : sensitiveWords,
                meta(latencyMs, 0, false));
    }

    private <T> List<AiProductRef> pickRefs(List<Long> ids, List<T> products,
                                            Function<T, Long> idGetter,
                                            Function<T, AiProductRef> mapper) {
        if (ids == null || ids.isEmpty() || products.isEmpty()) {
            return List.of();
        }
        List<AiProductRef> refs = new ArrayList<>();
        for (Long id : ids.stream().limit(5).toList()) {
            if (id == null) {
                continue;
            }
            products.stream()
                    .filter(product -> id.equals(idGetter.apply(product)))
                    .findFirst()
                    .map(mapper)
                    .ifPresent(refs::add);
        }
        return refs;
    }

    private Long validRefId(String type, Long refId, AiContextService.PlanContext context) {
        if (refId == null) {
            return null;
        }
        return switch (type) {
            case "line" -> context.lines().stream().anyMatch(line -> refId.equals(line.getId())) ? refId : null;
            case "hotel" -> context.hotels().stream().anyMatch(hotel -> refId.equals(hotel.getId())) ? refId : null;
            case "scenic" -> context.spots().stream().anyMatch(spot -> refId.equals(spot.getId())) ? refId : null;
            default -> null;
        };
    }

    private String resolveRefName(String type, Long refId, AiContextService.PlanContext context, String fallback) {
        if (refId == null) {
            return null;
        }
        return switch (type) {
            case "line" -> context.lines().stream().filter(line -> refId.equals(line.getId()))
                    .map(TourLine::getTitle).findFirst().orElse(fallback);
            case "hotel" -> context.hotels().stream().filter(hotel -> refId.equals(hotel.getId()))
                    .map(Hotel::getName).findFirst().orElse(fallback);
            case "scenic" -> context.spots().stream().filter(spot -> refId.equals(spot.getId()))
                    .map(ScenicSpot::getName).findFirst().orElse(fallback);
            default -> fallback;
        };
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "free";
        }
        String value = type.trim().toLowerCase();
        return PLAN_ITEM_TYPES.contains(value) ? value : "free";
    }

    private String normalizeRisk(String risk, int score) {
        if (risk != null) {
            String value = risk.trim().toUpperCase();
            if (Set.of("LOW", "MEDIUM", "HIGH").contains(value)) {
                return value;
            }
        }
        if (score >= 70) {
            return "HIGH";
        }
        return score >= 30 ? "MEDIUM" : "LOW";
    }

    private String normalizeSuggestion(String suggestion, String riskLevel) {
        if (suggestion != null) {
            String value = suggestion.trim().toUpperCase();
            if (Set.of("APPROVE", "REVIEW", "REJECT").contains(value)) {
                return value;
            }
        }
        return switch (riskLevel) {
            case "HIGH" -> "REJECT";
            case "MEDIUM" -> "REVIEW";
            default -> "APPROVE";
        };
    }

    private <T> T parseJson(String content, Class<T> type) {
        String json = extractJson(content);
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("AI JSON parse failed, content length={}", content == null ? 0 : content.length());
            throw new BusinessException(502, "AI 返回格式异常，请重试");
        }
    }

    private String extractJson(String content) {
        if (content == null) {
            return "";
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) {
                text = text.substring(firstNewline + 1);
            }
            int lastFence = text.lastIndexOf("```");
            if (lastFence >= 0) {
                text = text.substring(0, lastFence);
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        return text.trim();
    }

    private String planCacheKey(AiPlanRequest request) {
        String preferences = request.preferences() == null ? ""
                : request.preferences().stream().filter(p -> p != null && !p.isBlank())
                        .map(String::trim).sorted().reduce("", (a, b) -> a + "|" + b);
        String raw = request.destination() + "|" + request.normalizedDays() + "|"
                + request.budget() + "|" + request.departureCity() + "|"
                + request.travelStyle() + "|" + preferences + "|" + request.normalizedCompanions();
        return "plan:" + cacheService.hashKey(raw);
    }

    private AiMeta meta(long latencyMs, int grounded, boolean fallback) {
        return new AiMeta(properties.model(), false, latencyMs, grounded, fallback,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private AiMeta cachedMeta(AiMeta original) {
        return new AiMeta(original.model(), true, 0, original.groundedProducts(),
                original.fallback(), LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim();
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) + "…" : cleaned;
    }
}
