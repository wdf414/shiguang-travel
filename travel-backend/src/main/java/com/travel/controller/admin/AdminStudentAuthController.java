package com.travel.controller.admin;

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
@RequestMapping("/api/admin/student-auth")
public class AdminStudentAuthController {

    private static final Logger log = LoggerFactory.getLogger(AdminStudentAuthController.class);

    @Autowired
    private StudentAuthService studentAuthService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取待审核列表
     */
    @GetMapping("/pending")
    public Result<?> getPendingApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return studentAuthService.getPendingApplications(page, size);
    }

    /**
     * 获取所有申请列表
     */
    @GetMapping("/list")
    public Result<?> getAllApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 这里可以实现更复杂的查询逻辑
        return studentAuthService.getPendingApplications(page, size);
    }

    /**
     * 审核通过
     */
    @PostMapping("/approve/{id}")
    public Result<?> approveApplication(@PathVariable Long id) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        
        Long reviewerId = loginUser.getUserId();
        String reviewerName = getReviewerName(reviewerId);
        
        log.info("管理员审核通过学生认证: applicationId={}, reviewerId={}", id, reviewerId);
        
        return studentAuthService.approveApplication(id, reviewerId, reviewerName);
    }

    /**
     * 审核拒绝
     */
    @PostMapping("/reject/{id}")
    public Result<?> rejectApplication(
            @PathVariable Long id,
            @RequestParam String reason) {
        LoginUser loginUser = getCurrentUser();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            return Result.error("请填写拒绝原因");
        }
        
        Long reviewerId = loginUser.getUserId();
        String reviewerName = getReviewerName(reviewerId);
        
        log.info("管理员审核拒绝学生认证: applicationId={}, reviewerId={}, reason={}", 
                id, reviewerId, reason);
        
        return studentAuthService.rejectApplication(id, reviewerId, reviewerName, reason);
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/detail/{id}")
    public Result<?> getApplicationDetail(@PathVariable Long id) {
        StudentAuthApplication application = studentAuthService.getById(id);
        if (application == null) {
            return Result.error("申请记录不存在");
        }
        return Result.success(application);
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
    
    private String getReviewerName(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getNickname() != null ? user.getNickname() : "管理员";
    }
}
