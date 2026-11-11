package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.CampusLeader;
import com.travel.entity.LeaderCommission;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.mapper.CampusLeaderMapper;
import com.travel.mapper.LeaderCommissionMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.CampusLeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CampusLeaderServiceImpl extends ServiceImpl<CampusLeaderMapper, CampusLeader> implements CampusLeaderService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LeaderCommissionMapper leaderCommissionMapper;

    @Override
    public CampusLeader applyLeader(Long userId, CampusLeader leader) {
        // 检查用户是否是学生
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getIsStudent() == null || user.getIsStudent() != 1) {
            throw new BusinessException("仅学生用户可申请成为校园领队");
        }

        // 检查是否已申请过
        LambdaQueryWrapper<CampusLeader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CampusLeader::getUserId, userId);
        CampusLeader existing = this.getOne(wrapper);
        if (existing != null) {
            if (existing.getStatus() == 0) {
                throw new BusinessException("您已提交申请，请等待审核");
            }
            if (existing.getStatus() == 1) {
                throw new BusinessException("您已是认证领队");
            }
            // 如果之前被拒绝，允许重新申请
            existing.setSchool(leader.getSchool());
            existing.setMajor(leader.getMajor());
            existing.setGrade(leader.getGrade());
            existing.setDescription(leader.getDescription());
            existing.setStatus(0);
            this.updateById(existing);
            return existing;
        }

        leader.setUserId(userId);
        leader.setStatus(0); // 待审核
        leader.setCommissionRate(new BigDecimal("5.00")); // 默认5%
        leader.setTotalCommission(BigDecimal.ZERO);
        this.save(leader);

        // 同步更新用户的学校信息
        if (StringUtils.hasText(leader.getSchool())) {
            user.setSchool(leader.getSchool());
            user.setMajor(leader.getMajor());
            user.setGrade(leader.getGrade());
            userMapper.updateById(user);
        }

        return leader;
    }

    @Override
    public CampusLeader getLeaderByUserId(Long userId) {
        LambdaQueryWrapper<CampusLeader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CampusLeader::getUserId, userId);
        CampusLeader leader = this.getOne(wrapper);
        if (leader != null) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                leader.setNickname(user.getNickname());
                leader.setPhone(user.getPhone());
                leader.setAvatar(user.getAvatar());
            }
        }
        return leader;
    }

    @Override
    public Page<CampusLeader> getLeaderList(Page<CampusLeader> page, String school, Integer status) {
        LambdaQueryWrapper<CampusLeader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, CampusLeader::getStatus, status)
               .like(StringUtils.hasText(school), CampusLeader::getSchool, school)
               .orderByDesc(CampusLeader::getCreateTime);
        Page<CampusLeader> result = this.page(page, wrapper);

        // 填充用户信息
        result.getRecords().forEach(leader -> {
            User user = userMapper.selectById(leader.getUserId());
            if (user != null) {
                leader.setNickname(user.getNickname());
                leader.setPhone(user.getPhone());
                leader.setAvatar(user.getAvatar());
            }
        });

        return result;
    }

    @Override
    public void auditLeader(Long id, Integer status) {
        CampusLeader leader = this.getById(id);
        if (leader == null) {
            throw new BusinessException("领队不存在");
        }
        if (status != 1 && status != 2) {
            throw new BusinessException("无效的审核状态");
        }
        leader.setStatus(status);
        this.updateById(leader);
    }

    @Override
    public void setCommissionRate(Long id, BigDecimal rate) {
        CampusLeader leader = this.getById(id);
        if (leader == null) {
            throw new BusinessException("领队不存在");
        }
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("佣金比例需在0-100之间");
        }
        leader.setCommissionRate(rate);
        this.updateById(leader);
    }

    @Override
    public Page<LeaderCommission> getCommissions(Long leaderId, Page<LeaderCommission> page) {
        LambdaQueryWrapper<LeaderCommission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaderCommission::getLeaderId, leaderId)
               .orderByDesc(LeaderCommission::getCreateTime);
        return leaderCommissionMapper.selectPage(page, wrapper);
    }

    @Override
    public void deleteLeader(Long id) {
        CampusLeader leader = this.getById(id);
        if (leader == null) {
            throw new BusinessException("领队不存在");
        }
        this.removeById(id);
    }

    @Override
    public boolean isCertifiedLeader(Long userId) {
        LambdaQueryWrapper<CampusLeader> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CampusLeader::getUserId, userId)
               .eq(CampusLeader::getStatus, 1);
        return this.count(wrapper) > 0;
    }
}
