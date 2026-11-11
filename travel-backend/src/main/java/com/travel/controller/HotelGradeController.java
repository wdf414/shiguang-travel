package com.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.Result;
import com.travel.entity.HotelGrade;
import com.travel.service.HotelGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel-grades")
public class HotelGradeController {

    @Autowired
    private HotelGradeService hotelGradeService;

    @GetMapping
    public Result<Page<HotelGrade>> getHotelGradeList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        Page<HotelGrade> hotelPage = new Page<>(page, size);
        return Result.success(hotelGradeService.getHotelGradeList(hotelPage, city, keyword));
    }
}
