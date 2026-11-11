package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.Result;
import com.travel.entity.StudentAuthApplication;
import com.travel.entity.User;
import com.travel.mapper.StudentAuthApplicationMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.StudentAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StudentAuthServiceImpl extends ServiceImpl<StudentAuthApplicationMapper, StudentAuthApplication> 
        implements StudentAuthService {

    private static final Logger log = LoggerFactory.getLogger(StudentAuthServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<?> submitApplication(StudentAuthApplication application) {
        Long userId = application.getUserId();
        
        // 检查是否已有申请
        StudentAuthApplication existing = getOne(
            new LambdaQueryWrapper<StudentAuthApplication>()
                .eq(StudentAuthApplication::getUserId, userId)
                .orderByDesc(StudentAuthApplication::getCreateTime)
                .last("LIMIT 1")
        );
        
        if (existing != null) {
            if (existing.getStatus() == 0) {
                return Result.error("您已有待审核的申请，请勿重复提交");
            }
            if (existing.getStatus() == 1) {
                // 检查是否过期
                if (existing.getValidEndDate() != null && 
                    existing.getValidEndDate().isAfter(LocalDate.now())) {
                    return Result.error("您已通过学生认证，请勿重复申请");
                }
            }
        }
        
        // 设置初始状态
        application.setStatus(0);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        save(application);
        
        return Result.success("申请提交成功，请等待审核");
    }

    @Override
    public Result<?> getAuthStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("isStudent", user.getIsStudent());
        result.put("school", user.getSchool());
        result.put("major", user.getMajor());
        result.put("grade", user.getGrade());
        
        // 获取最新申请记录
        StudentAuthApplication application = getOne(
            new LambdaQueryWrapper<StudentAuthApplication>()
                .eq(StudentAuthApplication::getUserId, userId)
                .orderByDesc(StudentAuthApplication::getCreateTime)
                .last("LIMIT 1")
        );
        
        if (application != null) {
            result.put("authStatus", application.getStatus());
            result.put("application", application);
        } else {
            result.put("authStatus", 0);
        }
        
        return Result.success(result);
    }

    @Override
    @Transactional
    public Result<?> approveApplication(Long applicationId, Long reviewerId, String reviewerName) {
        StudentAuthApplication application = getById(applicationId);
        if (application == null) {
            return Result.error("申请记录不存在");
        }
        
        if (application.getStatus() != 0) {
            return Result.error("该申请已被处理");
        }
        
        // 更新申请状态
        application.setStatus(1);
        application.setReviewerId(reviewerId);
        application.setReviewerName(reviewerName);
        application.setReviewTime(LocalDateTime.now());
        application.setValidStartDate(LocalDate.now());
        application.setValidEndDate(LocalDate.now().plusYears(4)); // 默认4年有效期
        
        updateById(application);
        
        // 更新用户学生状态
        User user = userMapper.selectById(application.getUserId());
        if (user != null) {
            user.setIsStudent(1);
            user.setSchool(application.getSchoolName());
            user.setMajor(application.getMajor());
            user.setGrade(application.getGrade());
            userMapper.updateById(user);
        }
        
        log.info("学生认证申请已通过: applicationId={}, userId={}", applicationId, application.getUserId());
        return Result.success("审核通过");
    }

    @Override
    @Transactional
    public Result<?> rejectApplication(Long applicationId, Long reviewerId, String reviewerName, String reason) {
        StudentAuthApplication application = getById(applicationId);
        if (application == null) {
            return Result.error("申请记录不存在");
        }
        
        if (application.getStatus() != 0) {
            return Result.error("该申请已被处理");
        }
        
        // 更新申请状态
        application.setStatus(2);
        application.setReviewerId(reviewerId);
        application.setReviewerName(reviewerName);
        application.setReviewTime(LocalDateTime.now());
        application.setRejectReason(reason);
        
        updateById(application);
        
        log.info("学生认证申请已拒绝: applicationId={}, userId={}, reason={}", 
                applicationId, application.getUserId(), reason);
        return Result.success("审核已拒绝");
    }

    @Override
    public Result<?> getPendingApplications(Integer page, Integer size) {
        Page<StudentAuthApplication> pageParam = new Page<>(page, size);
        
        Page<StudentAuthApplication> result = page(pageParam,
            new LambdaQueryWrapper<StudentAuthApplication>()
                .eq(StudentAuthApplication::getStatus, 0)
                .orderByDesc(StudentAuthApplication::getCreateTime)
        );
        
        return Result.success(result);
    }

    @Override
    public Result<?> calculateStudentPrice(Long userId, Long lineId, Double originalPrice) {
        if (!isStudent(userId)) {
            return Result.success(Map.of(
                "isStudent", false,
                "originalPrice", originalPrice,
                "studentPrice", originalPrice,
                "discountAmount", 0
            ));
        }
        
        // 学生折扣率 95折
        BigDecimal price = BigDecimal.valueOf(originalPrice);
        BigDecimal discountRate = BigDecimal.valueOf(0.95);
        BigDecimal studentPrice = price.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
        
        // 计算优惠金额
        BigDecimal discountAmount = price.subtract(studentPrice);
        
        // 限制优惠金额范围
        if (discountAmount.compareTo(BigDecimal.valueOf(10)) < 0) {
            discountAmount = BigDecimal.valueOf(10);
            studentPrice = price.subtract(discountAmount);
        }
        if (discountAmount.compareTo(BigDecimal.valueOf(500)) > 0) {
            discountAmount = BigDecimal.valueOf(500);
            studentPrice = price.subtract(discountAmount);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("isStudent", true);
        result.put("originalPrice", originalPrice);
        result.put("studentPrice", studentPrice.doubleValue());
        result.put("discountAmount", discountAmount.doubleValue());
        result.put("discountRate", "95折");
        
        return Result.success(result);
    }

    @Override
    public boolean isStudent(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getIsStudent() != null && user.getIsStudent() == 1;
    }
}
