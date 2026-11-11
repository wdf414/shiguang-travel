package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.CampusLeader;
import com.travel.entity.Companion;
import com.travel.entity.CompanionMember;
import com.travel.entity.User;
import com.travel.exception.BusinessException;
import com.travel.mapper.CampusLeaderMapper;
import com.travel.mapper.CompanionMapper;
import com.travel.mapper.CompanionMemberMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.CompanionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanionServiceImpl extends ServiceImpl<CompanionMapper, Companion> implements CompanionService {

    @Autowired
    private CompanionMemberMapper companionMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CampusLeaderMapper campusLeaderMapper;

    @Override
    @Transactional
    public Companion createCompanion(Long userId, Companion companion) {
        companion.setUserId(userId);
        companion.setCurrentCount(1); // 创建者默认算一个人
        this.save(companion);

        // 添加创建者为成员
        CompanionMember member = new CompanionMember();
        member.setCompanionId(companion.getId());
        member.setUserId(userId);
        member.setStatus(1); // 已加入
        companionMemberMapper.insert(member);

        return companion;
    }

    @Override
    public Page<Companion> getCompanionList(Page<Companion> page, String keyword, Integer status,
                                             String school, String major, String grade) {
        LambdaQueryWrapper<Companion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Companion::getStatus, status)
               .and(StringUtils.hasText(keyword), w -> w
                       .like(Companion::getTitle, keyword)
                       .or()
                       .like(Companion::getContent, keyword))
               .eq(status == null, Companion::getStatus, 0) // 默认只显示招募中的
               .orderByDesc(Companion::getCreateTime);

        // 如果有学校筛选，先筛选 companion 自身的 school 字段
        if (StringUtils.hasText(school)) {
            wrapper.eq(Companion::getSchool, school);
        }

        Page<Companion> result = this.page(page, wrapper);

        // 填充发起人信息并进行额外筛选
        List<Companion> filteredRecords = result.getRecords().stream()
                .peek(this::fillCreatorInfo)
                .filter(c -> {
                    // 按发起人专业筛选
                    if (StringUtils.hasText(major) && !major.equals(c.getCreatorMajor())) {
                        return false;
                    }
                    // 按发起人年级筛选
                    if (StringUtils.hasText(grade) && !grade.equals(c.getCreatorGrade())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        result.setRecords(filteredRecords);
        return result;
    }

    @Override
    public Companion getCompanionDetail(Long id) {
        Companion companion = this.getById(id);
        if (companion == null || companion.getDeleted() == 1) {
            throw new BusinessException("结伴游不存在");
        }
        fillCreatorInfo(companion);
        return companion;
    }

    @Override
    public void updateCompanion(Long userId, Long id, Companion companion) {
        Companion existing = this.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("结伴游不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己发布的结伴游");
        }
        if (existing.getStatus() != 0) {
            throw new BusinessException("只有招募中的结伴游可以修改");
        }
        companion.setId(id);
        companion.setUserId(userId);
        this.updateById(companion);
    }

    @Override
    @Transactional
    public void deleteCompanion(Long userId, Long id) {
        Companion existing = this.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("结伴游不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己发布的结伴游");
        }
        if (existing.getStatus() != 0) {
            throw new BusinessException("只有招募中的结伴游可以删除");
        }

        // 逻辑删除
        existing.setDeleted(1);
        this.updateById(existing);

        // 删除相关成员记录
        LambdaQueryWrapper<CompanionMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CompanionMember::getCompanionId, id);
        companionMemberMapper.delete(memberWrapper);
    }

    @Override
    @Transactional
    public void joinCompanion(Long userId, Long companionId) {
        Companion companion = this.getById(companionId);
        if (companion == null || companion.getDeleted() == 1) {
            throw new BusinessException("结伴游不存在");
        }
        if (companion.getStatus() != 0) {
            throw new BusinessException("该结伴游无法加入");
        }
        if (companion.getCurrentCount() >= companion.getMemberCount()) {
            throw new BusinessException("人数已满");
        }

        // 检查是否已经是成员
        LambdaQueryWrapper<CompanionMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CompanionMember::getCompanionId, companionId)
                     .eq(CompanionMember::getUserId, userId);
        CompanionMember existingMember = companionMemberMapper.selectOne(memberWrapper);
        if (existingMember != null) {
            if (existingMember.getStatus() == 1) {
                throw new BusinessException("您已是该结伴游成员");
            } else if (existingMember.getStatus() == 2) {
                // 重新加入
                existingMember.setStatus(1);
                companionMemberMapper.updateById(existingMember);
            }
        } else {
            // 添加成员
            CompanionMember member = new CompanionMember();
            member.setCompanionId(companionId);
            member.setUserId(userId);
            member.setStatus(1);
            companionMemberMapper.insert(member);
        }

        // 更新当前人数
        companion.setCurrentCount(companion.getCurrentCount() + 1);
        if (companion.getCurrentCount() >= companion.getMemberCount()) {
            companion.setStatus(1); // 已成行
            // 拼团成功，自动生成群聊二维码入口（模拟）
            if (!StringUtils.hasText(companion.getGroupQrcode())) {
                companion.setGroupQrcode("/default-group-qrcode.png");
            }
        }
        this.updateById(companion);
    }

    @Override
    @Transactional
    public void quitCompanion(Long userId, Long companionId) {
        Companion companion = this.getById(companionId);
        if (companion == null || companion.getDeleted() == 1) {
            throw new BusinessException("结伴游不存在");
        }
        if (companion.getStatus() != 0) {
            throw new BusinessException("该结伴游无法退出");
        }

        LambdaQueryWrapper<CompanionMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(CompanionMember::getCompanionId, companionId)
                     .eq(CompanionMember::getUserId, userId);
        CompanionMember member = companionMemberMapper.selectOne(memberWrapper);
        if (member == null || member.getStatus() != 1) {
            throw new BusinessException("您不是该结伴游成员");
        }

        if (companion.getUserId().equals(userId)) {
            throw new BusinessException("创建者不能退出自己的结伴游");
        }

        // 更新成员状态
        member.setStatus(2); // 已退出
        companionMemberMapper.updateById(member);

        // 更新当前人数
        companion.setCurrentCount(companion.getCurrentCount() - 1);
        this.updateById(companion);
    }

    @Override
    public Page<Companion> getUserJoinedCompanions(Long userId, Page<Companion> page) {
        // 通过成员表关联查询
        List<CompanionMember> members = companionMemberMapper.selectList(
                new LambdaQueryWrapper<CompanionMember>().eq(CompanionMember::getUserId, userId));
        List<Long> companionIds = members.stream()
                .map(CompanionMember::getCompanionId)
                .filter(id -> {
                    Companion comp = this.getById(id);
                    return comp != null && comp.getDeleted() == 0;
                })
                .toList();

        if (companionIds.isEmpty()) {
            return new Page<>();
        }

        Page<Companion> result = this.page(page, new LambdaQueryWrapper<Companion>()
                .in(Companion::getId, companionIds)
                .orderByDesc(Companion::getCreateTime));

        result.getRecords().forEach(this::fillCreatorInfo);
        return result;
    }

    @Override
    public Page<Companion> getUserPublishedCompanions(Long userId, Page<Companion> page) {
        Page<Companion> result = this.page(page, new LambdaQueryWrapper<Companion>()
                .eq(Companion::getUserId, userId)
                .orderByDesc(Companion::getCreateTime));
        result.getRecords().forEach(this::fillCreatorInfo);
        return result;
    }

    @Override
    public List<CompanionMember> getCompanionMembers(Long companionId) {
        return companionMemberMapper.selectList(
                new LambdaQueryWrapper<CompanionMember>()
                        .eq(CompanionMember::getCompanionId, companionId)
                        .orderByAsc(CompanionMember::getCreateTime));
    }

    @Override
    public Companion getGroupChat(Long companionId) {
        Companion companion = this.getById(companionId);
        if (companion == null) {
            throw new BusinessException("结伴游不存在");
        }
        if (companion.getStatus() != 1) {
            throw new BusinessException("拼团尚未成功，暂无群聊入口");
        }
        // 如果没有二维码，生成一个默认的
        if (!StringUtils.hasText(companion.getGroupQrcode())) {
            companion.setGroupQrcode("/default-group-qrcode.png");
            this.updateById(companion);
        }
        return companion;
    }

    @Override
    public Page<Companion> getAllCompanions(Page<Companion> page, String keyword, Integer status) {
        LambdaQueryWrapper<Companion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Companion::getStatus, status)
               .and(StringUtils.hasText(keyword), w -> w
                       .like(Companion::getTitle, keyword)
                       .or()
                       .like(Companion::getContent, keyword))
               .orderByDesc(Companion::getCreateTime);
        Page<Companion> result = this.page(page, wrapper);
        result.getRecords().forEach(this::fillCreatorInfo);
        return result;
    }

    @Override
    public void adminUpdateStatus(Long id, Integer status) {
        Companion companion = this.getById(id);
        if (companion == null) {
            throw new BusinessException("结伴游不存在");
        }
        companion.setStatus(status);
        this.updateById(companion);
    }

    @Override
    @Transactional
    public void adminDeleteCompanion(Long id) {
        Companion companion = this.getById(id);
        if (companion == null) {
            throw new BusinessException("结伴游不存在");
        }
        this.removeById(id);
        // 删除相关成员
        companionMemberMapper.delete(new LambdaQueryWrapper<CompanionMember>()
                .eq(CompanionMember::getCompanionId, id));
    }

    /**
     * 填充发起人的用户信息和领队标识
     */
    private void fillCreatorInfo(Companion companion) {
        User user = userMapper.selectById(companion.getUserId());
        if (user != null) {
            companion.setCreatorName(user.getNickname());
            companion.setCreatorSchool(user.getSchool());
            companion.setCreatorMajor(user.getMajor());
            companion.setCreatorGrade(user.getGrade());
        }
        // 检查发起人是否是认证领队
        LambdaQueryWrapper<CampusLeader> leaderWrapper = new LambdaQueryWrapper<>();
        leaderWrapper.eq(CampusLeader::getUserId, companion.getUserId())
                     .eq(CampusLeader::getStatus, 1);
        long leaderCount = campusLeaderMapper.selectCount(leaderWrapper);
        companion.setIsLeader(leaderCount > 0 ? 1 : 0);
    }

    @Override
    public List<com.travel.dto.CompanionMemberDTO> getCompanionMemberDetails(Long companionId) {
        List<CompanionMember> members = getCompanionMembers(companionId);
        if (members.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        List<Long> userIds = members.stream().map(CompanionMember::getUserId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        java.util.Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        return members.stream().map(member -> {
            com.travel.dto.CompanionMemberDTO dto = new com.travel.dto.CompanionMemberDTO();
            dto.setId(member.getId());
            dto.setCompanionId(member.getCompanionId());
            dto.setUserId(member.getUserId());
            dto.setStatus(member.getStatus());
            dto.setCreateTime(member.getCreateTime());

            User user = userMap.get(member.getUserId());
            if (user != null) {
                dto.setNickname(user.getNickname());
                dto.setAvatar(user.getAvatar());
            }
            return dto;
        }).collect(Collectors.toList());
    }
}