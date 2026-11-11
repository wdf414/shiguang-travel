package com.travel.ai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.travel.ai.dto.AiChatResponse;
import com.travel.ai.dto.AiMeta;
import com.travel.ai.dto.AiModerationResponse;
import com.travel.ai.dto.AiPlanRequest;
import com.travel.ai.dto.AiPlanResponse;
import com.travel.ai.dto.AiProductRef;
import com.travel.entity.Hotel;
import com.travel.entity.ScenicSpot;
import com.travel.entity.TourLine;

/**
 * 大模型不可用时的确定性兜底。
 *
 * <p>结果会明确标记 fallback=true，前端显示“本地兜底”，不会冒充大模型输出。
 * 这样即使比赛现场网络波动，核心演示流程也不会中断。</p>
 */
@Service
public class AiLocalFallback {

    public AiPlanResponse plan(AiPlanRequest request, AiContextService.PlanContext context,
                               String model, long latencyMs) {
        int days = request.normalizedDays();
        int people = request.normalizedCompanions();
        List<AiPlanResponse.DayPlan> itinerary = new ArrayList<>();
        List<ScenicSpot> spots = context.spots();

        for (int day = 1; day <= days; day++) {
            List<AiPlanResponse.PlanItem> items = new ArrayList<>();
            if (day == 1) {
                items.add(new AiPlanResponse.PlanItem("09:00", "抵达 " + request.destination(),
                        "办理入住并熟悉周边环境，建议选择交通便利的住宿。", "transport", null, null, null));
                if (!context.hotels().isEmpty()) {
                    Hotel hotel = context.hotels().get(0);
                    items.add(new AiPlanResponse.PlanItem("14:00", "入住 " + hotel.getName(),
                            "平台酒店参考，评分 " + plain(hotel.getRating()) + "。", "hotel",
                            hotel.getId(), hotel.getName(), "实际房型与价格以详情页为准"));
                }
            }

            int firstIndex = Math.max(0, (day - 1) * 2);
            for (int offset = 0; offset < 2; offset++) {
                int index = firstIndex + offset;
                if (index < spots.size()) {
                    ScenicSpot spot = spots.get(index);
                    items.add(new AiPlanResponse.PlanItem(offset == 0 ? "09:30" : "14:30",
                            spot.getName(),
                            "位于" + blankTo(spot.getCity(), request.destination()) + "，可安排 2-3 小时游览。",
                            "scenic", spot.getId(), spot.getName(), blankTo(spot.getTag(), null)));
                }
            }
            if (items.size() < 3) {
                items.add(new AiPlanResponse.PlanItem("18:00", "晚餐与自由活动",
                        "尝试当地特色小吃，控制预算并注意饮食卫生。", "food", null, null, null));
            }

            String theme = day == 1 ? "抵达与适应" : (day == days ? "收尾与返程" : "深度游览");
            itinerary.add(new AiPlanResponse.DayPlan(day,
                    "第 " + day + " 天 · " + blankTo(request.destination(), "目的地"),
                    theme, items));
        }

        BigDecimal hotelUnit = context.hotels().isEmpty() || context.hotels().get(0).getPriceStart() == null
                ? new BigDecimal("200")
                : context.hotels().get(0).getPriceStart();
        BigDecimal transport = context.lines().isEmpty() || context.lines().get(0).getPrice() == null
                ? new BigDecimal("300")
                : context.lines().get(0).getPrice();
        BigDecimal hotel = hotelUnit.multiply(BigDecimal.valueOf(Math.max(1, days - 1)));
        BigDecimal tickets = BigDecimal.valueOf(80L * days);
        BigDecimal food = BigDecimal.valueOf(100L * days);
        BigDecimal other = BigDecimal.valueOf(50L * days);
        BigDecimal perPerson = transport.add(hotel).add(tickets).add(food).add(other);
        BigDecimal total = perPerson.multiply(BigDecimal.valueOf(people));

        List<String> tips = List.of(
                "当前为大模型不可用时的本地兜底结果，未调用 AI，仅用于保证演示流程可用。",
                "价格为平台数据参考值，实际库存、房型和优惠以下单页为准。",
                "旺季出行建议提前 3-7 天预订，并确认退改规则。");

        StringBuilder summary = new StringBuilder("已根据平台数据生成 " + days + " 天本地参考行程，可作为临时方案。");
        if (context.lines().isEmpty()) {
            summary.append("平台暂未收录该目的地在售线路；");
        }
        if (context.hotels().isEmpty()) {
            summary.append("平台暂未收录该目的地酒店；");
        }
        if (context.spots().isEmpty()) {
            summary.append("平台暂未收录该目的地景点；");
        }

        return new AiPlanResponse(
                summary.toString(),
                days,
                itinerary,
                new AiPlanResponse.Budget(transport, hotel, tickets, food, other, total, perPerson, "CNY"),
                context.lines().stream().limit(3).map(this::toLineRef).toList(),
                context.hotels().stream().limit(3).map(this::toHotelRef).toList(),
                tips,
                meta(model, latencyMs, context, true));
    }

    public AiChatResponse chat(String message, AiContextService.ChatContext context,
                               String model, long latencyMs) {
        StringBuilder reply = new StringBuilder();
        reply.append("AI 服务当前不可用，我先给出平台内的检索结果（本地兜底，不是大模型回答）：\n");
        if (context.references().isEmpty()) {
            reply.append("暂时没有检索到相关产品，请换个目的地或关键词试试。");
        } else {
            context.references().stream().limit(5).forEach(ref -> {
                reply.append("· ").append(ref.name());
                if (ref.subtitle() != null && !ref.subtitle().isBlank()) {
                    reply.append("（").append(ref.subtitle()).append("）");
                }
                if (ref.price() != null) {
                    reply.append("，参考价 ¥").append(plain(ref.price()));
                }
                reply.append("\n");
            });
            reply.append("\n你可以点击下方卡片查看详情，或稍后重试 AI 问答。");
        }
        List<AiProductRef> refs = context.references().stream().limit(8).toList();
        return new AiChatResponse(reply.toString(), refs,
                new AiMeta(model, false, latencyMs, refs.size(), true,
                        LocalDateTime.now().toString()));
    }

    public AiModerationResponse moderation(List<String> sensitiveWords, String model, long latencyMs) {
        boolean hasWords = sensitiveWords != null && !sensitiveWords.isEmpty();
        return new AiModerationResponse(
                hasWords ? "MEDIUM" : "LOW",
                hasWords ? 55 : 10,
                hasWords ? List.of("敏感词命中") : List.of("未发现明显风险"),
                hasWords
                        ? List.of("命中平台敏感词：" + String.join("、", sensitiveWords))
                        : List.of("AI 服务不可用，仅完成敏感词规则初筛，建议人工复核"),
                "REVIEW",
                sensitiveWords == null ? List.of() : sensitiveWords,
                new AiMeta(model, false, latencyMs, 0, true, LocalDateTime.now().toString()));
    }

    private AiMeta meta(String model, long latencyMs, AiContextService.PlanContext context, boolean fallback) {
        int grounded = context.lines().size() + context.hotels().size() + context.spots().size();
        return new AiMeta(model, false, latencyMs, grounded, fallback, LocalDateTime.now().toString());
    }

    private AiProductRef toLineRef(TourLine line) {
        return new AiProductRef("line", line.getId(), line.getTitle(),
                line.getDestination() + " · " + (line.getDays() == null ? "?" : line.getDays()) + "天",
                line.getPrice(), line.getCoverImage(), "/line/" + line.getId());
    }

    private AiProductRef toHotelRef(Hotel hotel) {
        return new AiProductRef("hotel", hotel.getId(), hotel.getName(),
                hotel.getCity() + " · 评分 " + plain(hotel.getRating()),
                hotel.getPriceStart(), hotel.getCoverImage(), "/hotels/" + hotel.getId());
    }

    private String plain(BigDecimal value) {
        return value == null ? "-" : value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
