package com.travel.controller;

import com.travel.common.Result;
import com.travel.entity.StudentAuthApplication;
import com.travel.entity.User;
import com.travel.mapper.UserMapper;
import com.travel.security.LoginUser;
import com.travel.service.StudentAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentAuthController {

    private static final Logger log = LoggerFactory.getLogger(StudentAuthController.class);

    @Autowired
    private StudentAuthService studentAuthService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 提交学生认证申请
     */
    @PostMapping("/auth/apply")
    public Result<?> submitApplication(@RequestBody StudentAuthApplication application) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        
        Long userId = loginUser.getUserId();
        User user = userMapper.selectById(userId);
        
        application.setUserId(userId);
        application.setUserName(user != null ? user.getNickname() : "");
        application.setUserPhone(user != null ? user.getPhone() : "");
        
        log.info("提交学生认证申请: userId={}", userId);
        return studentAuthService.submitApplication(application);
    }

    /**
     * 获取当前用户的学生认证状态
     */
    @GetMapping("/auth/status")
    public Result<?> getAuthStatus() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        
        return studentAuthService.getAuthStatus(loginUser.getUserId());
    }

    /**
     * 计算学生价格
     */
    @GetMapping("/price/calculate")
    public Result<?> calculateStudentPrice(
            @RequestParam Long lineId,
            @RequestParam Double originalPrice) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        
        return studentAuthService.calculateStudentPrice(
                loginUser.getUserId(), lineId, originalPrice);
    }

    /**
     * 检查是否为学生用户
     */
    @GetMapping("/check")
    public Result<?> checkStudent() {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.success(false);
        }
        
        boolean isStudent = studentAuthService.isStudent(loginUser.getUserId());
        return Result.success(isStudent);
    }

    /**
     * 更新学生信息（仅已认证学生）
     */
    @PutMapping("/info")
    public Result<?> updateStudentInfo(@RequestBody User userInfo) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        
        Long userId = loginUser.getUserId();
        
        if (!studentAuthService.isStudent(userId)) {
            return Result.error("您不是认证学生用户");
        }
        
        User user = userMapper.selectById(userId);
        if (userInfo.getSchool() != null) {
            user.setSchool(userInfo.getSchool());
        }
        if (userInfo.getMajor() != null) {
            user.setMajor(userInfo.getMajor());
        }
        if (userInfo.getGrade() != null) {
            user.setGrade(userInfo.getGrade());
        }
        
        userMapper.updateById(user);
        return Result.success("信息更新成功");
    }

    private LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        
        return null;
    }
}
