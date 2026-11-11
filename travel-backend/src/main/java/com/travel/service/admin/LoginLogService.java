package com.travel.service.admin;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.entity.LoginLog;
import com.travel.entity.User;
import com.travel.mapper.LoginLogMapper;
import com.travel.mapper.UserMapper;

@Service
public class LoginLogService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final LoginLogMapper loginLogMapper;
    private final UserMapper userMapper;

    public LoginLogService(RedisTemplate<String, Object> redisTemplate, 
                          LoginLogMapper loginLogMapper, 
                          UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.loginLogMapper = loginLogMapper;
        this.userMapper = userMapper;
    }

    // 获取登录日志列表
    public Map<String, Object> getLoginLogs(int page, int size, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        // 分页查询登录日志
        Page<LoginLog> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();
        
        // 如果有关键字，关联用户表查询
        if (keyword != null && !keyword.isEmpty()) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .like(User::getPhone, keyword)
                .or()
                .like(User::getNickname, keyword));
            
            if (!users.isEmpty()) {
                List<Long> userIds = users.stream().map(User::getId).toList();
                wrapper.in(LoginLog::getUserId, userIds);
            } else {
                // 没有匹配的用户，直接返回空结果
                result.put("records", new ArrayList<>());
                result.put("total", 0);
                result.put("page", page);
                result.put("size", size);
                return result;
            }
        }
        
        wrapper.orderByDesc(LoginLog::getLoginTime);
        Page<LoginLog> logPage = loginLogMapper.selectPage(pageObj, wrapper);
        
        // 获取用户信息
        List<Long> userIds = logPage.getRecords().stream()
            .map(LoginLog::getUserId)
            .distinct()
            .toList();
        
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            for (User user : users) {
                userMap.put(user.getId(), user);
            }
        }
        
        // 转换为前端需要的格式
        List<Map<String, Object>> records = new ArrayList<>();
        for (LoginLog log : logPage.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", log.getId());
            item.put("userId", log.getUserId());
            item.put("loginTime", log.getLoginTime() != null ? 
                log.getLoginTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            item.put("ip", log.getIp());
            item.put("device", parseDevice(log.getUserAgent()));
            item.put("status", log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败");
            
            User user = userMap.get(log.getUserId());
            if (user != null) {
                item.put("phone", user.getPhone());
                item.put("nickname", user.getNickname());
            } else {
                item.put("phone", "");
                item.put("nickname", "");
            }
            
            records.add(item);
        }
        
        result.put("records", records);
        result.put("total", logPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        
        return result;
    }
    
    private String parseDevice(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }
        if (userAgent.toLowerCase().contains("mobile") || 
            userAgent.toLowerCase().contains("android") ||
            userAgent.toLowerCase().contains("iphone") ||
            userAgent.toLowerCase().contains("ipad")) {
            return "Mobile";
        }
        return "PC";
    }

    private LocalDate getEffectiveNow() {
        return LocalDate.now();
    }

    // 获取登录统计
    public Map<String, Object> getLoginStats(int days) {
        Map<String, Object> result = new HashMap<>();
        List<String> dates = new ArrayList<>();
        List<Integer> loginCounts = new ArrayList<>();
        
        LocalDate effectiveNow = getEffectiveNow();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = effectiveNow.minusDays(i);
            dates.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            
            String dateKey = date.format(DateTimeFormatter.BASIC_ISO_DATE);
            String key = "travel:stats:login_hour:" + dateKey;
            Map<Object, Object> hourMap = redisTemplate.opsForHash().entries(key);
            
            int totalCount = 0;
            for (Object count : hourMap.values()) {
                totalCount += Integer.parseInt(count.toString());
            }
            loginCounts.add(totalCount);
        }
        
        result.put("dates", dates);
        result.put("loginCounts", loginCounts);
        
        return result;
    }
}
