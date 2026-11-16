package com.travel.ai.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.travel.ai.dto.AiProductRef;
import com.travel.entity.Hotel;
import com.travel.entity.HotelGrade;
import com.travel.entity.ScenicSpot;
import com.travel.entity.TourLine;
import com.travel.mapper.HotelGradeMapper;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.ScenicSpotMapper;
import com.travel.mapper.TourLineMapper;

/**
 * RAG 的“检索”环节：从平台数据库召回可售线路、酒店和景点，拼成受控上下文。
 *
 * <p>只把产品公开信息传给模型，不发送用户手机号、身份证、订单等个人数据。</p>
 */
@Service
public class AiContextService {
    private static final int MAX_CONTEXT_CHARS = 12000;

    private static final long PLACES_CACHE_TTL_MS = 10 * 60 * 1000L;

    private final TourLineMapper tourLineMapper;
    private final HotelMapper hotelMapper;
    private final HotelGradeMapper hotelGradeMapper;
    private final ScenicSpotMapper scenicSpotMapper;

    private volatile List<String> knownPlacesCache = List.of();
    private volatile long knownPlacesLoadedAt = 0L;

    public AiContextService(TourLineMapper tourLineMapper,
                            HotelMapper hotelMapper,
                            HotelGradeMapper hotelGradeMapper,
                            ScenicSpotMapper scenicSpotMapper) {
        this.tourLineMapper = tourLineMapper;
        this.hotelMapper = hotelMapper;
        this.hotelGradeMapper = hotelGradeMapper;
        this.scenicSpotMapper = scenicSpotMapper;
    }

    public PlanContext buildPlanContext(String destination, int days, BigDecimal budget, String departureCity) {
        List<TourLine> lines = queryLines(destination, departureCity, budget, 8, false);
        List<Hotel> hotels = queryHotels(destination, 6, false);
        List<ScenicSpot> spots = queryScenicSpots(destination, 12, false);

        StringBuilder text = new StringBuilder(2048);
        text.append("## 平台真实数据（只能引用下列产品 ID）\n");
        text.append("### 可售线路\n");
        if (lines.isEmpty()) {
            text.append("（该目的地暂无在售线路）\n");
        } else {
            for (TourLine line : lines) {
                text.append("- [L").append(line.getId()).append("] ")
                        .append(safe(line.getTitle(), 80))
                        .append(" | 目的地:").append(safe(line.getDestination(), 30))
                        .append(" | 出发:").append(safe(line.getDepartureCity(), 30))
                        .append(" | ").append(line.getDays() == null ? "?" : line.getDays()).append("天")
                        .append(" | 价格:¥").append(plain(line.getPrice()))
                        .append(" | 评分:").append(plain(line.getRating()))
                        .append(" | 标签:").append(safe(line.getTags(), 60))
                        .append("\n  简介:").append(safe(line.getDescription(), 220))
                        .append("\n");
            }
        }

        text.append("### 平台酒店\n");
        if (hotels.isEmpty()) {
            text.append("（该目的地暂无酒店数据）\n");
        } else {
            for (Hotel hotel : hotels) {
                text.append("- [H").append(hotel.getId()).append("] ")
                        .append(safe(hotel.getName(), 80))
                        .append(" | 城市:").append(safe(hotel.getCity(), 30))
                        .append(" | 地址:").append(safe(hotel.getAddress(), 80))
                        .append(" | 评分:").append(plain(hotel.getRating()))
                        .append(" | 星级:").append(hotel.getStar() == null ? "-" : hotel.getStar())
                        .append(" | 起价:¥").append(plain(hotel.getPriceStart()))
                        .append(" | 标签:").append(safe(hotel.getTags(), 60))
                        .append("\n");
            }
        }

        text.append("### 平台景点\n");
        if (spots.isEmpty()) {
            text.append("（该目的地暂无景点数据）\n");
        } else {
            for (ScenicSpot spot : spots) {
                text.append("- [S").append(spot.getId()).append("] ")
                        .append(safe(spot.getName(), 80))
                        .append(" | 城市:").append(safe(spot.getCity(), 30))
                        .append(" | 区县:").append(safe(spot.getDistrict(), 30))
                        .append(" | 类型:").append(safe(spot.getType(), 30))
                        .append(" | 标签:").append(safe(spot.getTag(), 50))
                        .append(" | 地址:").append(safe(spot.getAddress(), 100))
                        .append("\n");
            }
        }

        return new PlanContext(truncate(text.toString()), lines, hotels, spots);
    }

    public ChatContext buildChatContext(String message) {
        String keyword = normalizeKeyword(message);
        List<TourLine> lines = queryLines(keyword, null, null, 5, true);
        List<Hotel> hotels = queryHotels(keyword, 4, true);
        List<ScenicSpot> spots = queryScenicSpots(keyword, 6, true);

        StringBuilder text = new StringBuilder(2048);
        text.append("## 平台可检索数据\n");
        text.append("### 线路\n");
        lines.forEach(line -> text.append("- [L").append(line.getId()).append("] ")
                .append(safe(line.getTitle(), 60)).append(" | ")
                .append(safe(line.getDestination(), 20)).append(" | ")
                .append(line.getDays() == null ? "?" : line.getDays()).append("天 | ¥")
                .append(plain(line.getPrice())).append("\n"));
        text.append("### 酒店\n");
        hotels.forEach(hotel -> text.append("- [H").append(hotel.getId()).append("] ")
                .append(safe(hotel.getName(), 60)).append(" | ")
                .append(safe(hotel.getCity(), 20)).append(" | ¥")
                .append(plain(hotel.getPriceStart())).append(" | 评分")
                .append(plain(hotel.getRating())).append("\n"));
        text.append("### 景点\n");
        spots.forEach(spot -> text.append("- [S").append(spot.getId()).append("] ")
                .append(safe(spot.getName(), 60)).append(" | ")
                .append(safe(spot.getCity(), 20)).append(" | ")
                .append(safe(spot.getTag(), 30)).append("\n"));

        List<AiProductRef> refs = new ArrayList<>();
        lines.forEach(line -> refs.add(toLineRef(line)));
        hotels.forEach(hotel -> refs.add(toHotelRef(hotel)));
        spots.forEach(spot -> refs.add(toScenicRef(spot)));
        return new ChatContext(truncate(text.toString()), dedupe(refs));
    }

    /**
     * 数据库中存在同名重复产品（不同 ID），前端只展示一次，避免引用列表刷屏。
     */
    public List<AiProductRef> dedupe(List<AiProductRef> refs) {
        Map<String, AiProductRef> unique = new LinkedHashMap<>();
        for (AiProductRef ref : refs) {
            unique.putIfAbsent(ref.type() + "|" + ref.name(), ref);
        }
        return new ArrayList<>(unique.values());
    }

    private List<TourLine> queryLines(String destination, String departureCity, BigDecimal budget,
                                      int limit, boolean allowFallback) {
        LambdaQueryWrapper<TourLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TourLine::getStatus, 1);
        if (StringUtils.hasText(destination)) {
            wrapper.and(w -> w.like(TourLine::getDestination, destination)
                    .or().like(TourLine::getTitle, destination));
        }
        if (StringUtils.hasText(departureCity)) {
            wrapper.eq(TourLine::getDepartureCity, departureCity);
        }
        if (budget != null && budget.signum() > 0) {
            // 允许 1.5 倍预算的线路进入上下文，让模型有机会给出“略超预算”的替代方案。
            wrapper.le(TourLine::getPrice, budget.multiply(new BigDecimal("1.5")));
        }
        wrapper.orderByDesc(TourLine::getRating).last("LIMIT " + limit);
        List<TourLine> result = tourLineMapper.selectList(wrapper);
        if (result.isEmpty() && allowFallback) {
            // 聊天场景需要通用上下文；行程规划场景则保持“该目的地无线路”的真实状态。
            result = tourLineMapper.selectList(new LambdaQueryWrapper<TourLine>()
                    .eq(TourLine::getStatus, 1)
                    .orderByDesc(TourLine::getSales)
                    .last("LIMIT " + limit));
        }
        return result;
    }

    private List<Hotel> queryHotels(String destination, int limit, boolean allowFallback) {
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hotel::getStatus, 1);
        if (StringUtils.hasText(destination)) {
            wrapper.and(w -> w.like(Hotel::getCity, destination)
                    .or().like(Hotel::getName, destination));
        }
        wrapper.orderByDesc(Hotel::getRating).last("LIMIT " + limit);
        List<Hotel> result = hotelMapper.selectList(wrapper);

        // hotel 表只有少量精选酒店，其余在 hote_grade 导入表中；
        // 这里做数据源兜底，避免“桂林行程推荐三亚酒店”的幻觉式上下文。
        if (result.isEmpty() && StringUtils.hasText(destination)) {
            List<HotelGrade> grades = hotelGradeMapper.selectList(new LambdaQueryWrapper<HotelGrade>()
                    .and(w -> w.like(HotelGrade::getCity, destination)
                            .or().like(HotelGrade::getName, destination))
                    .orderByDesc(HotelGrade::getRating)
                    .last("LIMIT " + limit));
            result = grades.stream().map(this::fromGrade).toList();
        }

        if (result.isEmpty() && allowFallback) {
            result = hotelMapper.selectList(new LambdaQueryWrapper<Hotel>()
                    .eq(Hotel::getStatus, 1)
                    .orderByDesc(Hotel::getRating)
                    .last("LIMIT " + limit));
        }
        return result;
    }

    private List<ScenicSpot> queryScenicSpots(String destination, int limit, boolean allowFallback) {
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(destination)) {
            wrapper.and(w -> w.like(ScenicSpot::getName, destination)
                    .or().like(ScenicSpot::getCity, destination)
                    .or().like(ScenicSpot::getSearchCity, destination)
                    .or().like(ScenicSpot::getTag, destination));
        }
        wrapper.orderByAsc(ScenicSpot::getCity).last("LIMIT " + limit);
        List<ScenicSpot> result = scenicSpotMapper.selectList(wrapper);
        if (result.isEmpty() && allowFallback) {
            result = scenicSpotMapper.selectList(new LambdaQueryWrapper<ScenicSpot>()
                    .orderByAsc(ScenicSpot::getCity)
                    .last("LIMIT " + limit));
        }
        return result;
    }

    private Hotel fromGrade(HotelGrade grade) {
        Hotel hotel = new Hotel();
        hotel.setId(grade.getId());
        hotel.setName(grade.getName());
        hotel.setCity(grade.getCity());
        hotel.setAddress(grade.getAddress());
        hotel.setRating(grade.getRating() == null ? new BigDecimal("4.5") : grade.getRating());
        hotel.setStar(4);
        hotel.setPriceStart(new BigDecimal("199"));
        hotel.setTags(grade.getArea());
        hotel.setCoverImage(StringUtils.hasText(grade.getCoverImage())
                ? grade.getCoverImage()
                : "/api/photos/pick?seed=hotel-" + grade.getId());
        hotel.setStatus(1);
        return hotel;
    }

    /**
     * 从用户问题中提取一个较短的检索词，避免把整句问题当作 SQL LIKE 条件。
     */
    private String normalizeKeyword(String message) {
        if (!StringUtils.hasText(message)) {
            return null;
        }
        // 先匹配平台已有的城市/省份，避免把“三亚三天怎么玩”整句拿去 LIKE。
        for (String place : knownPlaces()) {
            if (message.contains(place)) {
                return place;
            }
        }
        String cleaned = message.replaceAll("[\\p{Punct}\\s]", " ").trim();
        if (cleaned.length() > 30) {
            cleaned = cleaned.substring(0, 30);
        }
        return cleaned;
    }

    /**
     * 汇总平台出现过的城市与省份，优先城市、再省份，长名称优先匹配。
     * 结果缓存 10 分钟，避免每次聊天都扫全表。
     */
    private List<String> knownPlaces() {
        long now = System.currentTimeMillis();
        if (!knownPlacesCache.isEmpty() && now - knownPlacesLoadedAt < PLACES_CACHE_TTL_MS) {
            return knownPlacesCache;
        }
        LinkedHashSet<String> places = new LinkedHashSet<>();
        try {
            hotelMapper.selectList(new LambdaQueryWrapper<Hotel>().select(Hotel::getCity))
                    .forEach(hotel -> places.add(hotel.getCity()));
        } catch (Exception ignored) {
        }
        try {
            scenicSpotMapper.selectObjs(new QueryWrapper<ScenicSpot>().select("DISTINCT city"))
                    .forEach(city -> places.add(String.valueOf(city)));
        } catch (Exception ignored) {
        }
        try {
            hotelGradeMapper.selectObjs(new QueryWrapper<HotelGrade>().select("DISTINCT 城市"))
                    .forEach(city -> places.add(String.valueOf(city)));
        } catch (Exception ignored) {
        }
        try {
            places.addAll(tourLineMapper.selectDestinations());
        } catch (Exception ignored) {
        }
        List<String> sorted = places.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .filter(place -> place.length() >= 2 && place.length() <= 20)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
        knownPlacesCache = sorted;
        knownPlacesLoadedAt = now;
        return sorted;
    }

    public AiProductRef toLineRef(TourLine line) {
        String subtitle = safe(line.getDestination(), 20) + " · "
                + (line.getDays() == null ? "?" : line.getDays()) + "天";
        return new AiProductRef("line", line.getId(), line.getTitle(), subtitle,
                line.getPrice(), line.getCoverImage(), "/line/" + line.getId());
    }

    public AiProductRef toHotelRef(Hotel hotel) {
        String subtitle = safe(hotel.getCity(), 20) + " · 评分 " + plain(hotel.getRating());
        return new AiProductRef("hotel", hotel.getId(), hotel.getName(), subtitle,
                hotel.getPriceStart(), hotel.getCoverImage(), "/hotels/" + hotel.getId());
    }

    public AiProductRef toScenicRef(ScenicSpot spot) {
        String category = safe(spot.getTag(), 20);
        if (!StringUtils.hasText(category) || "scope".equalsIgnoreCase(category)) {
            category = safe(spot.getType(), 20);
        }
        if (!StringUtils.hasText(category) || "scope".equalsIgnoreCase(category)) {
            category = "景点";
        }
        String subtitle = safe(spot.getCity(), 20) + " · " + category;
        String image = StringUtils.hasText(spot.getCoverImage())
                ? spot.getCoverImage()
                : "/api/scenic-spot/photo?name=" + URLEncoder.encode(safe(spot.getName(), 80), StandardCharsets.UTF_8);
        String url = "/scenic-search?keyword=" + URLEncoder.encode(safe(spot.getName(), 80), StandardCharsets.UTF_8);
        return new AiProductRef("scenic", spot.getId(), spot.getName(), subtitle, null, image, url);
    }

    private String safe(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replace("\r", " ").replace("\n", " ").replace("|", "／").trim();
        return cleaned.length() > maxLength ? cleaned.substring(0, maxLength) + "…" : cleaned;
    }

    private String plain(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private String truncate(String value) {
        return value.length() > MAX_CONTEXT_CHARS ? value.substring(0, MAX_CONTEXT_CHARS) : value;
    }

    public record PlanContext(String text, List<TourLine> lines, List<Hotel> hotels, List<ScenicSpot> spots) {
    }

    public record ChatContext(String text, List<AiProductRef> references) {
    }
}
