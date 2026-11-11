package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.common.Result;
import com.travel.entity.StudentAuthApplication;

public interface StudentAuthService extends IService<StudentAuthApplication> {
    
    /**
     * 提交学生认证申请
     */
    Result<?> submitApplication(StudentAuthApplication application);
    
    /**
     * 获取当前用户的认证状态
     */
    Result<?> getAuthStatus(Long userId);
    
    /**
     * 审核通过
     */
    Result<?> approveApplication(Long applicationId, Long reviewerId, String reviewerName);
    
    /**
     * 审核拒绝
     */
    Result<?> rejectApplication(Long applicationId, Long reviewerId, String reviewerName, String reason);
    
    /**
     * 获取待审核列表
     */
    Result<?> getPendingApplications(Integer page, Integer size);
    
    /**
     * 计算学生价格
     */
    Result<?> calculateStudentPrice(Long userId, Long lineId, Double originalPrice);
    
    /**
     * 检查用户是否为学生
     */
    boolean isStudent(Long userId);
}
