package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.CampusLeader;
import com.travel.entity.LeaderCommission;

import java.math.BigDecimal;
import java.util.List;

public interface CampusLeaderService extends IService<CampusLeader> {

    /**
     * 申请成为校园领队
     */
    CampusLeader applyLeader(Long userId, CampusLeader leader);

    /**
     * 获取当前用户的领队信息
     */
    CampusLeader getLeaderByUserId(Long userId);

    /**
     * 获取认证领队列表
     */
    Page<CampusLeader> getLeaderList(Page<CampusLeader> page, String school, Integer status);

    /**
     * 审核领队申请
     */
    void auditLeader(Long id, Integer status);

    /**
     * 设置佣金比例
     */
    void setCommissionRate(Long id, BigDecimal rate);

    /**
     * 获取领队佣金明细
     */
    Page<LeaderCommission> getCommissions(Long leaderId, Page<LeaderCommission> page);

    /**
     * 删除领队
     */
    void deleteLeader(Long id);

    /**
     * 检查用户是否是已认证领队
     */
    boolean isCertifiedLeader(Long userId);
}
