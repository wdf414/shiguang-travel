package com.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.Result;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import com.travel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @GetMapping
    public Result<Page<Hotel>> getHotelList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword) {
        Page<Hotel> hotelPage = new Page<>(page, size);
        return Result.success(hotelService.getHotelList(hotelPage, city, keyword));
    }

    @GetMapping("/{id}")
    public Result<Hotel> getHotelDetail(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelDetail(id);
        if (hotel == null) return Result.error("酒店不存在");
        return Result.success(hotel);
    }

    @GetMapping("/{id}/rooms")
    public Result<List<HotelRoom>> getHotelRooms(@PathVariable Long id) {
        return Result.success(hotelService.getHotelRooms(id));
    }
}
