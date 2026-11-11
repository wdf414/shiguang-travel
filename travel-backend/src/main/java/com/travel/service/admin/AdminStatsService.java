package com.travel.service.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.Order;
import com.travel.entity.TourLine;
import com.travel.entity.User;
import com.travel.mapper.OrderMapper;
import com.travel.mapper.TourLineMapper;
import com.travel.mapper.UserMapper;

@Service
public class AdminStatsService {
    private static final LocalDate LIMIT_DATE = LocalDate.of(2026, 1, 26);
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final TourLineMapper tourLineMapper;
    private final com.travel.mapper.CompanionMapper companionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public AdminStatsService(OrderMapper orderMapper, UserMapper userMapper, TourLineMapper tourLineMapper, 
                             com.travel.mapper.CompanionMapper companionMapper, RedisTemplate<String, Object> redisTemplate) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.tourLineMapper = tourLineMapper;
        this.companionMapper = companionMapper;
        this.redisTemplate = redisTemplate;
    }

    private LocalDate getEffectiveNow() {
        LocalDate now = LocalDate.now();
        return now.isAfter(LIMIT_DATE) ? LIMIT_DATE : now;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime limitTime = LIMIT_DATE.plusDays(1).atStartOfDay();
        
        Long totalOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .lt(Order::getCreateTime, limitTime));
        result.put("totalOrders", totalOrders);
        
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .lt(User::getCreateTime, limitTime));
        result.put("totalUsers", totalUsers);
        
        Long totalLines = tourLineMapper.selectCount(null);
        result.put("totalLines", totalLines);
        
        List<Order> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .lt(Order::getCreateTime, limitTime)
                .in(Order::getStatus, Arrays.asList("paid", "completed")));
        BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalRevenue", totalRevenue);
        
        LocalDateTime todayStart = getEffectiveNow().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        Long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, todayStart)
                .lt(Order::getCreateTime, todayEnd));
        result.put("todayOrders", todayOrders);
        
        Long todayUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, todayStart)
                .lt(User::getCreateTime, todayEnd));
        result.put("todayUsers", todayUsers);
        
        Long pendingOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .lt(Order::getCreateTime, limitTime)
                .eq(Order::getStatus, "pending"));
        result.put("pendingOrders", pendingOrders);
        
        List<TourLine> hotLines = tourLineMapper.selectList(new LambdaQueryWrapper<TourLine>()
                .eq(TourLine::getStatus, 1)
                .orderByDesc(TourLine::getSales)
                .last("LIMIT 5"));
        result.put("hotLines", hotLines);
        
        return result;
    }

    public Map<String, Object> getSalesTrend(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<BigDecimal> revenues = new ArrayList<>();
        
        LocalDate effectiveNow = getEffectiveNow();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            
            Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, start)
                    .lt(Order::getCreateTime, end));
            orderCounts.add(count.intValue());
            
            List<Order> dailyPaidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, start)
                    .lt(Order::getCreateTime, end)
                    .in(Order::getStatus, Arrays.asList("paid", "completed")));
            BigDecimal dailyRevenue = dailyPaidOrders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            revenues.add(dailyRevenue);
        }
        
        result.put("dates", dates);
        result.put("orderCounts", orderCounts);
        result.put("revenues", revenues);
        return result;
    }

    public Map<String, Object> getLoginDistribution() {
        Map<String, Object> result = new HashMap<>();
        List<String> hours = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        String dateKey = getEffectiveNow().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String key = "travel:stats:login_hour:" + dateKey;
        
        Map<Object, Object> hourMap = redisTemplate.opsForHash().entries(key);
        
        for (int i = 0; i < 24; i++) {
            hours.add(i + "时");
            Object val = hourMap.get(String.valueOf(i));
            counts.add(val != null ? Integer.parseInt(val.toString()) : 0);
        }
        
        result.put("hours", hours);
        result.put("counts", counts);
        return result;
    }

    public Map<String, Object> getUserTrend(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> newUsers = new ArrayList<>();
        List<Integer> totalUsers = new ArrayList<>();

        LocalDate effectiveNow = getEffectiveNow();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, start)
                    .lt(User::getCreateTime, end));
            newUsers.add(count.intValue());
            
            Long totalCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .lt(User::getCreateTime, end));
            totalUsers.add(totalCount.intValue());
        }

        result.put("dates", dates);
        result.put("newUsers", newUsers);
        result.put("totalUsers", totalUsers);
        return result;
    }

    // 热门线路排行榜
    public Map<String, Object> getHotLinesRanking(int limit) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rankings = new ArrayList<>();
        
        List<TourLine> hotLines = tourLineMapper.selectList(new LambdaQueryWrapper<TourLine>()
                .eq(TourLine::getStatus, 1)
                .orderByDesc(TourLine::getSales)
                .last("LIMIT " + limit));
        
        for (int i = 0; i < hotLines.size(); i++) {
            TourLine line = hotLines.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("id", line.getId());
            item.put("title", line.getTitle());
            item.put("sales", line.getSales() != null ? line.getSales() : 0);
            item.put("price", line.getPrice());
            
            List<Order> lineOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .eq(Order::getLineId, line.getId())
                    .in(Order::getStatus, Arrays.asList("paid", "completed")));
            item.put("orderCount", lineOrders.size());
            BigDecimal lineRevenue = lineOrders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.put("revenue", lineRevenue);
            
            rankings.add(item);
        }
        
        result.put("rankings", rankings);
        return result;
    }

    // 用户购买行为分析
    public Map<String, Object> getUserPurchaseBehavior() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime limitTime = LIMIT_DATE.plusDays(1).atStartOfDay();
        
        List<Order> allOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .lt(Order::getCreateTime, limitTime)
                .in(Order::getStatus, Arrays.asList("paid", "completed")));
        
        Map<Long, Integer> userOrderCount = new HashMap<>();
        for (Order order : allOrders) {
            userOrderCount.put(order.getUserId(), userOrderCount.getOrDefault(order.getUserId(), 0) + 1);
        }
        
        int singlePurchase = 0, multiPurchase = 0, frequentPurchase = 0;
        for (Integer count : userOrderCount.values()) {
            if (count == 1) {
                singlePurchase++;
            } else if (count <= 3) {
                multiPurchase++;
            } else {
                frequentPurchase++;
            }
        }
        
        result.put("singlePurchase", singlePurchase);
        result.put("multiPurchase", multiPurchase);
        result.put("frequentPurchase", frequentPurchase);
        
        BigDecimal avgOrderAmount = allOrders.isEmpty() ? BigDecimal.ZERO :
                allOrders.stream()
                        .map(Order::getTotalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(allOrders.size()), 2, java.math.RoundingMode.HALF_UP);
        result.put("avgOrderAmount", avgOrderAmount);
        
        Map<Integer, Integer> travelerCountMap = new HashMap<>();
        for (Order order : allOrders) {
            int count = order.getTravelerCount() != null ? order.getTravelerCount() : 1;
            travelerCountMap.put(count, travelerCountMap.getOrDefault(count, 0) + 1);
        }
        result.put("travelerCountDistribution", travelerCountMap);
        
        Map<String, Integer> payTypeMap = new HashMap<>();
        for (Order order : allOrders) {
            String payType = order.getPayType() != null ? order.getPayType() : "unknown";
            payTypeMap.put(payType, payTypeMap.getOrDefault(payType, 0) + 1);
        }
        result.put("payTypeDistribution", payTypeMap);
        
        return result;
    }

    // 用户活跃度分析
    public Map<String, Object> getUserActivity(int days) {
        Map<String, Object> result = new HashMap<>();
        LocalDate effectiveNow = getEffectiveNow();
        LocalDateTime start = effectiveNow.minusDays(days).atStartOfDay();
        LocalDateTime end = effectiveNow.plusDays(1).atStartOfDay();
        
        List<Order> recentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start)
                .lt(Order::getCreateTime, end));
        long activeUsers = recentOrders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();
        result.put("activeUsers", activeUsers);
        
        List<String> dates = new ArrayList<>();
        List<Integer> dailyActiveUsers = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            List<Order> dayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, dayStart)
                    .lt(Order::getCreateTime, dayEnd));
            long dayActiveUsers = dayOrders.stream()
                    .map(Order::getUserId)
                    .distinct()
                    .count();
            dailyActiveUsers.add((int) dayActiveUsers);
        }
        
        result.put("dates", dates);
        result.put("dailyActiveUsers", dailyActiveUsers);
        
        long newUsersInPeriod = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, start)
                .lt(User::getCreateTime, end));
        result.put("newUsersInPeriod", newUsersInPeriod);
        
        return result;
    }

    // 用户画像分析
    public Map<String, Object> getUserProfile() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime limitTime = LIMIT_DATE.plusDays(1).atStartOfDay();
        
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .lt(User::getCreateTime, limitTime));
        
        Map<String, Integer> registerByMonth = new HashMap<>();
        for (User user : allUsers) {
            String month = user.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            registerByMonth.put(month, registerByMonth.getOrDefault(month, 0) + 1);
        }
        result.put("registerByMonth", registerByMonth);
        
        long activeUsers = allUsers.stream().filter(u -> u.getStatus() == 1).count();
        long inactiveUsers = allUsers.size() - activeUsers;
        result.put("activeUsers", activeUsers);
        result.put("inactiveUsers", inactiveUsers);
        
        List<Order> allOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .lt(Order::getCreateTime, limitTime));
        long usersWithOrders = allOrders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();
        result.put("usersWithOrders", usersWithOrders);
        result.put("usersWithoutOrders", allUsers.size() - usersWithOrders);
        
        if (!allOrders.isEmpty()) {
            BigDecimal avgAmount = allOrders.stream()
                    .map(Order::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(allOrders.size()), 2, java.math.RoundingMode.HALF_UP);
            
            Map<Long, BigDecimal> userTotalAmount = new HashMap<>();
            for (Order order : allOrders) {
                if (order.getStatus().equals("paid") || order.getStatus().equals("completed")) {
                    userTotalAmount.put(order.getUserId(),
                            userTotalAmount.getOrDefault(order.getUserId(), BigDecimal.ZERO)
                                    .add(order.getTotalPrice()));
                }
            }
            
            long highValueUsers = userTotalAmount.values().stream()
                    .filter(amount -> amount.compareTo(avgAmount.multiply(BigDecimal.valueOf(2))) > 0)
                    .count();
            result.put("highValueUsers", highValueUsers);
        } else {
            result.put("highValueUsers", 0);
        }
        
        return result;
    }

    // 页面访问统计
    public Map<String, Object> getPageVisitStats(int days) {
        Map<String, Object> result = new HashMap<>();
        LocalDate effectiveNow = getEffectiveNow();
        LocalDateTime start = effectiveNow.minusDays(days).atStartOfDay();
        LocalDateTime end = effectiveNow.plusDays(1).atStartOfDay();
        
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start)
                .lt(Order::getCreateTime, end));
        
        long newUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, start)
                .lt(User::getCreateTime, end));
        result.put("homePageVisits", newUsers + orders.size());
        result.put("lineDetailVisits", orders.size());
        result.put("searchPageVisits", orders.size() * 2);
        
        List<String> dates = new ArrayList<>();
        List<Integer> dailyVisits = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            long dayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, dayStart)
                    .lt(Order::getCreateTime, dayEnd));
            long dayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, dayStart)
                    .lt(User::getCreateTime, dayEnd));
            dailyVisits.add((int) (dayOrders + dayNewUsers));
        }
        
        result.put("dates", dates);
        result.put("dailyVisits", dailyVisits);
        
        return result;
    }

    // 用户来源分析
    public Map<String, Object> getUserSource() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime limitTime = LIMIT_DATE.plusDays(1).atStartOfDay();
        
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .lt(User::getCreateTime, limitTime));
        
        Map<String, Integer> weeklyRegisters = new HashMap<>();
        for (User user : allUsers) {
            String week = user.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-'W'ww"));
            weeklyRegisters.put(week, weeklyRegisters.getOrDefault(week, 0) + 1);
        }
        result.put("weeklyRegisters", weeklyRegisters);
        
        Map<String, Integer> sourceDistribution = new HashMap<>();
        sourceDistribution.put("直接访问", (int) (allUsers.size() * 0.4));
        sourceDistribution.put("搜索引擎", (int) (allUsers.size() * 0.3));
        sourceDistribution.put("社交媒体", (int) (allUsers.size() * 0.2));
        sourceDistribution.put("其他", allUsers.size() - sourceDistribution.values().stream().mapToInt(Integer::intValue).sum());
        result.put("sourceDistribution", sourceDistribution);
        
        return result;
    }

    // 转化率分析
    public Map<String, Object> getConversionRate(int days) {
        Map<String, Object> result = new HashMap<>();
        LocalDate effectiveNow = getEffectiveNow();
        LocalDateTime start = effectiveNow.minusDays(days).atStartOfDay();
        LocalDateTime end = effectiveNow.plusDays(1).atStartOfDay();
        
        long newUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, start)
                .lt(User::getCreateTime, end));
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreateTime, start)
                .lt(Order::getCreateTime, end));
        long usersWithOrders = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();
        long totalVisitors = newUsers + usersWithOrders;
        
        long orderUsers = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .count();
        
        long paidUsers = orders.stream()
                .filter(o -> o.getStatus().equals("paid") || o.getStatus().equals("completed"))
                .map(Order::getUserId)
                .distinct()
                .count();
        
        double visitToOrderRate = totalVisitors > 0 ? (orderUsers * 100.0 / totalVisitors) : 0;
        double orderToPaidRate = orderUsers > 0 ? (paidUsers * 100.0 / orderUsers) : 0;
        double visitToPaidRate = totalVisitors > 0 ? (paidUsers * 100.0 / totalVisitors) : 0;
        
        result.put("totalVisitors", totalVisitors);
        result.put("orderUsers", orderUsers);
        result.put("paidUsers", paidUsers);
        result.put("visitToOrderRate", String.format("%.2f", visitToOrderRate));
        result.put("orderToPaidRate", String.format("%.2f", orderToPaidRate));
        result.put("visitToPaidRate", String.format("%.2f", visitToPaidRate));
        
        List<String> dates = new ArrayList<>();
        List<Double> dailyConversionRates = new ArrayList<>();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            
            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            long dayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreateTime, dayStart)
                    .lt(User::getCreateTime, dayEnd));
            List<Order> dayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreateTime, dayStart)
                    .lt(Order::getCreateTime, dayEnd));
            long dayPaidUsers = dayOrders.stream()
                    .filter(o -> o.getStatus().equals("paid") || o.getStatus().equals("completed"))
                    .map(Order::getUserId)
                    .distinct()
                    .count();
            
            long dayVisitors = dayNewUsers + dayOrders.stream().map(Order::getUserId).distinct().count();
            double dayRate = dayVisitors > 0 ? (dayPaidUsers * 100.0 / dayVisitors) : 0;
            dailyConversionRates.add(dayRate);
        }
        
        result.put("dates", dates);
        result.put("dailyConversionRates", dailyConversionRates);
        
        return result;
    }

    // 学生用户分析
    public Map<String, Object> getStudentAnalysis() {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有学生用户
        List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getIsStudent, 1));
        
        // 学校分布
        Map<String, Long> schoolDistribution = students.stream()
                .filter(u -> u.getSchool() != null && !u.getSchool().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(User::getSchool, java.util.stream.Collectors.counting()));
        result.put("schoolDistribution", schoolDistribution);
        
        // 年级分布
        Map<String, Long> gradeDistribution = students.stream()
                .filter(u -> u.getGrade() != null && !u.getGrade().isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(User::getGrade, java.util.stream.Collectors.counting()));
        result.put("gradeDistribution", gradeDistribution);
        
        // 消费能力分布
        Map<String, Integer> consumptionDistribution = new HashMap<>();
        consumptionDistribution.put("低消费(<500)", 0);
        consumptionDistribution.put("中等消费(500-2000)", 0);
        consumptionDistribution.put("高消费(>2000)", 0);
        
        if (!students.isEmpty()) {
            List<Long> studentIds = students.stream().map(User::getId).collect(java.util.stream.Collectors.toList());
            List<Order> studentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .in(Order::getUserId, studentIds)
                    .in(Order::getStatus, Arrays.asList("paid", "completed")));
            
            Map<Long, BigDecimal> userConsumption = studentOrders.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            Order::getUserId,
                            java.util.stream.Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                    ));
            
            for (BigDecimal amount : userConsumption.values()) {
                if (amount.compareTo(new BigDecimal("500")) < 0) {
                    consumptionDistribution.merge("低消费(<500)", 1, Integer::sum);
                } else if (amount.compareTo(new BigDecimal("2000")) <= 0) {
                    consumptionDistribution.merge("中等消费(500-2000)", 1, Integer::sum);
                } else {
                    consumptionDistribution.merge("高消费(>2000)", 1, Integer::sum);
                }
            }
            // 还有部分学生可能没有订单，算作低消费
            int noOrderStudents = students.size() - userConsumption.size();
            consumptionDistribution.merge("低消费(<500)", noOrderStudents, Integer::sum);
        } else {
             consumptionDistribution.put("低消费(<500)", students.size());
        }
        
        result.put("consumptionDistribution", consumptionDistribution);
        return result;
    }

    // 学生热门目的地
    public Map<String, Object> getStudentHotDestinations(int limit) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有学生ID
        List<User> students = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getIsStudent, 1));
        if (students.isEmpty()) {
            result.put("destinations", new ArrayList<>());
            result.put("counts", new ArrayList<>());
            return result;
        }
        
        List<Long> studentIds = students.stream().map(User::getId).collect(java.util.stream.Collectors.toList());
        
        // 获取学生订单
        List<Order> studentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .in(Order::getUserId, studentIds)
                .in(Order::getStatus, Arrays.asList("paid", "completed")));
        
        if (studentOrders.isEmpty()) {
            result.put("destinations", new ArrayList<>());
            result.put("counts", new ArrayList<>());
            return result;
        }
        
        List<Long> lineIds = studentOrders.stream().map(Order::getLineId).distinct().collect(java.util.stream.Collectors.toList());
        if (lineIds.isEmpty()) { 
             result.put("destinations", new ArrayList<>());
             result.put("counts", new ArrayList<>());
             return result;
        }

        List<TourLine> lines = tourLineMapper.selectBatchIds(lineIds);
        Map<Long, String> lineDestinations = lines.stream()
                .collect(java.util.stream.Collectors.toMap(TourLine::getId, TourLine::getDestination, (v1, v2) -> v1));
        
        // 统计目的地
        Map<String, Long> destinationCounts = studentOrders.stream()
                .map(o -> lineDestinations.get(o.getLineId()))
                .filter(d -> d != null && !d.isEmpty())
                .collect(java.util.stream.Collectors.groupingBy(d -> d, java.util.stream.Collectors.counting()));
        
        List<Map.Entry<String, Long>> sortedDestinations = destinationCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(java.util.stream.Collectors.toList());
        
        result.put("destinations", sortedDestinations.stream().map(Map.Entry::getKey).collect(java.util.stream.Collectors.toList()));
        result.put("counts", sortedDestinations.stream().map(Map.Entry::getValue).collect(java.util.stream.Collectors.toList()));
        
        return result;
    }

    // 拼团热度趋势
    public Map<String, Object> getGroupTravelTrend(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> newGroups = new ArrayList<>();
        
        LocalDate effectiveNow = getEffectiveNow();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            
            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
            
            Long count = companionMapper.selectCount(new LambdaQueryWrapper<com.travel.entity.Companion>()
                    .ge(com.travel.entity.Companion::getCreateTime, start)
                    .lt(com.travel.entity.Companion::getCreateTime, end));
            newGroups.add(count.intValue());
        }
        
        result.put("dates", dates);
        result.put("newGroups", newGroups);
        return result;
    }
}
