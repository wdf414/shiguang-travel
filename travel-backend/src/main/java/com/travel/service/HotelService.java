package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.Hotel;
import com.travel.entity.HotelRoom;
import java.util.List;

public interface HotelService extends IService<Hotel> {
    
    Page<Hotel> getHotelList(Page<Hotel> page, String city, String keyword);
    
    Hotel getHotelDetail(Long id);
    
    List<HotelRoom> getHotelRooms(Long hotelId);
}
