package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.HotelGrade;

public interface HotelGradeService extends IService<HotelGrade> {
    Page<HotelGrade> getHotelGradeList(Page<HotelGrade> page, String city, String keyword);
}
