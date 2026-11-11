package com.travel.controller;

import com.travel.common.Result;
import com.travel.dto.LoginDTO;
import com.travel.dto.RegisterDTO;
import com.travel.security.LoginUser;
import com.travel.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return Result.success(userService.login(dto, ip, userAgent));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(userService.getUserInfo(loginUser.getUserId()));
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody Map<String, Object> body) {
        String nickname = (String) body.get("nickname");
        String email = (String) body.get("email");
        String avatar = (String) body.get("avatar");
        Boolean isStudent = null;
        Object isStudentVal = body.get("isStudent");
        if (isStudentVal instanceof Boolean) {
            isStudent = (Boolean) isStudentVal;
        } else if (isStudentVal instanceof Number) {
            isStudent = ((Number) isStudentVal).intValue() == 1;
        } else if (isStudentVal instanceof String) {
            String s = ((String) isStudentVal).trim();
            if (!s.isEmpty()) {
                isStudent = "1".equals(s) || "true".equalsIgnoreCase(s);
            }
        }
        userService.updateUserInfo(loginUser.getUserId(), nickname, email, avatar, isStudent);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody Map<String, String> body) {
        userService.updatePassword(loginUser.getUserId(), body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
