package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.Companion;
import com.travel.entity.CompanionMember;

import java.util.List;

public interface CompanionService extends IService<Companion> {

    /**
     * 创建结伴游
     */
    Companion createCompanion(Long userId, Companion companion);

    /**
     * 获取结伴游列表（支持校园筛选）
     */
    Page<Companion> getCompanionList(Page<Companion> page, String keyword, Integer status,
                                     String school, String major, String grade);

    /**
     * 获取结伴游详情
     */
    Companion getCompanionDetail(Long id);

    /**
     * 更新结伴游
     */
    void updateCompanion(Long userId, Long id, Companion companion);

    /**
     * 删除结伴游
     */
    void deleteCompanion(Long userId, Long id);

    /**
     * 加入结伴游
     */
    void joinCompanion(Long userId, Long companionId);

    /**
     * 退出结伴游
     */
    void quitCompanion(Long userId, Long companionId);

    /**
     * 获取用户参与的结伴游列表
     */
    Page<Companion> getUserJoinedCompanions(Long userId, Page<Companion> page);

    /**
     * 获取用户发布的结伴游列表
     */
    Page<Companion> getUserPublishedCompanions(Long userId, Page<Companion> page);

    /**
     * 获取结伴游成员列表
     */
    List<CompanionMember> getCompanionMembers(Long companionId);
    
    /**
     * 获取结伴游成员详情列表（包含用户信息）
     */
    List<com.travel.dto.CompanionMemberDTO> getCompanionMemberDetails(Long companionId);

    /**
     * 获取群聊入口信息
     */
    Companion getGroupChat(Long companionId);

    /**
     * 管理员获取所有结伴列表
     */
    Page<Companion> getAllCompanions(Page<Companion> page, String keyword, Integer status);

    /**
     * 管理员更新结伴状态
     */
    void adminUpdateStatus(Long id, Integer status);

    /**
     * 管理员删除结伴
     */
    void adminDeleteCompanion(Long id);
}