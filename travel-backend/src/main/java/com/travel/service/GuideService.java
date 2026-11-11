package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.Guide;

public interface GuideService extends IService<Guide> {

    /**
     * 创建导游攻略
     */
    Guide createGuide(Long userId, Guide guide);

    /**
     * 获取导游攻略列表
     */
    Page<Guide> getGuideList(Page<Guide> page, String keyword, Integer status);

    /**
     * 获取导游攻略详情
     */
    Guide getGuideDetail(Long id);

    /**
     * 更新导游攻略
     */
    void updateGuide(Long userId, Long id, Guide guide);

    /**
     * 删除导游攻略
     */
    void deleteGuide(Long userId, Long id);

    /**
     * 发布导游攻略
     */
    void publishGuide(Long userId, Long id);

    /**
     * 获取用户发布的导游攻略列表
     */
    Page<Guide> getUserGuides(Long userId, Page<Guide> page);

    /**
     * 增加浏览数
     */
    void increaseViewCount(Long id);

    /**
     * 点赞/取消点赞
     */
    void toggleLike(Long userId, Long id);
}