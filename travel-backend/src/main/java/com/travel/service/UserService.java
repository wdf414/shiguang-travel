package com.travel.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.dto.LoginDTO;
import com.travel.dto.RegisterDTO;
import com.travel.entity.LoginLog;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.mapper.LoginLogMapper;
import com.travel.mapper.UserMapper;
import com.travel.security.JwtUtil;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginLogMapper loginLogMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.admin-register-key:}")
    private String adminRegisterKey;

    public Map<String, Object> login(LoginDTO dto, String ip, String userAgent) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));
        if (user == null) {
            throw new BusinessException("手机号或密码错误");
        }

        // 统一使用 BCrypt 校验，禁止任何“万能密码”后门。
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole());

        // 保存登录日志到数据库
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(user.getId());
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setIp(ip != null ? ip : "127.0.0.1");
            loginLog.setUserAgent(userAgent != null ? userAgent : "Unknown");
            loginLog.setStatus(1);
            loginLogMapper.insert(loginLog);
        } catch (Exception ignored) {
            // 登录日志记录失败不影响登录
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            String dateKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String key = "travel:stats:login_hour:" + dateKey;
            redisTemplate.opsForHash().increment(key, String.valueOf(now.getHour()), 1L);
        } catch (Exception ignored) {
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", toUserVO(user));
        return result;
    }

    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, dto.getPhone()));
        if (count > 0) {
            throw new BusinessException("手机号已注册");
        }
        
        User user = new User();
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 管理员密钥来自环境变量；未配置时关闭管理员自助注册。
        if (dto.getAdminKey() != null && !dto.getAdminKey().isBlank()) {
            if (adminRegisterKey == null || adminRegisterKey.isBlank()) {
                throw new BusinessException("管理员注册未启用，请联系平台管理员");
            }
            if (!constantTimeEquals(adminRegisterKey, dto.getAdminKey())) {
                throw new BusinessException("管理员密钥错误");
            }
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }
        
        user.setStatus(1);
        userMapper.insert(user);
    }

    /** 按主键查询用户，供审核记录等场景读取昵称。 */
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }

    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    public void updateUserInfo(Long userId, String nickname, String email, String avatar, Boolean isStudent) {
        User user = new User();
        user.setId(userId);
        user.setNickname(nickname);
        user.setEmail(email);
        if (avatar != null && !avatar.isEmpty()) {
            user.setAvatar(avatar);
        }
        if (isStudent != null) {
            user.setIsStudent(isStudent ? 1 : 0);
        }
        userMapper.updateById(user);
    }

    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private Map<String, Object> toUserVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        vo.put("phone", user.getPhone());
        vo.put("nickname", user.getNickname());
        vo.put("email", user.getEmail());
        vo.put("avatar", user.getAvatar());
        vo.put("role", user.getRole());
        vo.put("isStudent", user.getIsStudent() != null && user.getIsStudent() == 1);
        return vo;
    }
}
