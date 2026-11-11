package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.HotelGrade;
import com.travel.mapper.HotelGradeMapper;
import com.travel.service.HotelGradeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HotelGradeServiceImpl extends ServiceImpl<HotelGradeMapper, HotelGrade> implements HotelGradeService {

    @Override
    public Page<HotelGrade> getHotelGradeList(Page<HotelGrade> page, String city, String keyword) {
        LambdaQueryWrapper<HotelGrade> wrapper = new LambdaQueryWrapper<>();
        
        // Match city if provided
        if (StringUtils.hasText(city)) {
            wrapper.eq(HotelGrade::getCity, city);
        }
        
        // Match keyword in name if provided
        if (StringUtils.hasText(keyword)) {
            wrapper.like(HotelGrade::getName, keyword);
        }
        
        // Sort by ranking (assuming 1 is best)
        wrapper.orderByAsc(HotelGrade::getRanking);
        
        return baseMapper.selectPage(page, wrapper);
    }
}
