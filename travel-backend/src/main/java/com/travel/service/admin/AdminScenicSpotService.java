package com.travel.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.entity.ScenicSpot;
import com.travel.exception.BusinessException;
import com.travel.mapper.ScenicSpotMapper;

import java.time.LocalDateTime;

@Service
public class AdminScenicSpotService {
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    public PageResult<ScenicSpot> getPage(int page, int size, String keyword, String city) {
        Page<ScenicSpot> p = new Page<>(page, size);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ScenicSpot::getName, keyword)
                   .or().like(ScenicSpot::getAddress, keyword));
        }
        if (StringUtils.hasText(city)) {
            wrapper.and(w -> w.like(ScenicSpot::getCity, city)
                   .or().like(ScenicSpot::getSearchCity, city));
        }
        
        wrapper.orderByAsc(ScenicSpot::getId);
        Page<ScenicSpot> result = scenicSpotMapper.selectPage(p, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public ScenicSpot getDetail(Long id) {
        ScenicSpot spot = scenicSpotMapper.selectById(id);
        if (spot == null) {
            throw new BusinessException("景点不存在");
        }
        return spot;
    }

    public void create(ScenicSpot spot) {
        spot.setCreateTime(LocalDateTime.now());
        spot.setUpdateTime(LocalDateTime.now());
        scenicSpotMapper.insert(spot);
    }

    public void update(ScenicSpot spot) {
        ScenicSpot existing = scenicSpotMapper.selectById(spot.getId());
        if (existing == null) {
            throw new BusinessException("景点不存在");
        }
        spot.setUpdateTime(LocalDateTime.now());
        scenicSpotMapper.updateById(spot);
    }

    public void delete(Long id) {
        scenicSpotMapper.deleteById(id);
    }
}
