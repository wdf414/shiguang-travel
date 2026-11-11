package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.Guide;
import com.travel.exception.BusinessException;
import com.travel.mapper.GuideMapper;
import com.travel.service.GuideService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class GuideServiceImpl extends ServiceImpl<GuideMapper, Guide> implements GuideService {

    @Override
    public Guide createGuide(Long userId, Guide guide) {
        guide.setUserId(userId);
        guide.setViewCount(0);
        guide.setLikeCount(0);
        guide.setStatus(0); // 草稿状态
        this.save(guide);
        return guide;
    }

    @Override
    public Page<Guide> getGuideList(Page<Guide> page, String keyword, Integer status) {
        LambdaQueryWrapper<Guide> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Guide::getStatus, status)
               .and(StringUtils.hasText(keyword), w -> w
                       .like(Guide::getTitle, keyword)
                       .or()
                       .like(Guide::getContent, keyword))
               .orderByDesc(Guide::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public Guide getGuideDetail(Long id) {
        Guide guide = this.getById(id);
        if (guide == null || guide.getDeleted() == 1) {
            throw new BusinessException("导游攻略不存在");
        }
        return guide;
    }

    @Override
    public void updateGuide(Long userId, Long id, Guide guide) {
        Guide existing = this.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("导游攻略不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("只能修改自己发布的导游攻略");
        }
        guide.setId(id);
        guide.setUserId(userId);
        this.updateById(guide);
    }

    @Override
    public void deleteGuide(Long userId, Long id) {
        Guide existing = this.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("导游攻略不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己发布的导游攻略");
        }
        // 逻辑删除
        existing.setDeleted(1);
        this.updateById(existing);
    }

    @Override
    public void publishGuide(Long userId, Long id) {
        Guide existing = this.getById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("导游攻略不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("只能发布自己创建的导游攻略");
        }
        existing.setStatus(1); // 设置为已发布
        this.updateById(existing);
    }

    @Override
    public Page<Guide> getUserGuides(Long userId, Page<Guide> page) {
        return this.page(page, new LambdaQueryWrapper<Guide>()
                .eq(Guide::getUserId, userId)
                .orderByDesc(Guide::getCreateTime));
    }

    @Override
    public void increaseViewCount(Long id) {
        Guide guide = this.getById(id);
        if (guide != null) {
            guide.setViewCount(guide.getViewCount() + 1);
            this.updateById(guide);
        }
    }

    @Override
    public void toggleLike(Long userId, Long id) {
        Guide guide = this.getById(id);
        if (guide == null || guide.getDeleted() == 1) {
            throw new BusinessException("导游攻略不存在");
        }
        // 简单实现，实际应用中可能需要记录具体点赞用户
        guide.setLikeCount(guide.getLikeCount() + 1);
        this.updateById(guide);
    }
}