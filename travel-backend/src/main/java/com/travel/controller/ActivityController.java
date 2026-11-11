package com.travel.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.common.Result;
import com.travel.entity.Activity;
import com.travel.entity.User;
import com.travel.mapper.UserMapper;
import com.travel.security.LoginUser;
import com.travel.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<Activity>> getActiveActivities() {
        // 获取当前登录用户
        boolean isStudent = checkIsStudent();
        
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, 1)
                .ge(Activity::getEndTime, LocalDateTime.now())
                .orderByDesc(Activity::getCreateTime);
        
        List<Activity> activities = activityService.list(wrapper);
        
        // 如果不是学生，过滤掉仅学生可见的活动
        if (!isStudent) {
            activities = activities.stream()
                    .filter(activity -> activity.getStudentOnly() == null || activity.getStudentOnly() == 0)
                    .collect(Collectors.toList());
        }
        
        return Result.success(activities);
    }

    @GetMapping("/student-only")
    public Result<List<Activity>> getStudentOnlyActivities() {
        // 检查是否为学生
        if (!checkIsStudent()) {
            return Result.error("仅学生用户可查看");
        }
        
        List<Activity> activities = activityService.list(
                new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getStatus, 1)
                        .eq(Activity::getStudentOnly, 1)
                        .ge(Activity::getEndTime, LocalDateTime.now())
                        .orderByDesc(Activity::getCreateTime)
        );
        
        return Result.success(activities);
    }

    private boolean checkIsStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) principal;
            User user = userMapper.selectById(loginUser.getUserId());
            return user != null && user.getIsStudent() != null && user.getIsStudent() == 1;
        }
        
        return false;
    }
}
