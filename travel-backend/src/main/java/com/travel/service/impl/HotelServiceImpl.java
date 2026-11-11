package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.Hotel;
import com.travel.entity.HotelGrade;
import com.travel.entity.HotelRoom;
import com.travel.mapper.HotelGradeMapper;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.HotelRoomMapper;
import com.travel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements HotelService {

    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    @Autowired
    private HotelGradeMapper hotelGradeMapper;

    @Override
    public Page<Hotel> getHotelList(Page<Hotel> page, String city, String keyword) {
        LambdaQueryWrapper<Hotel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hotel::getStatus, 1);
        wrapper.eq(StringUtils.hasText(city), Hotel::getCity, city);
        wrapper.and(StringUtils.hasText(keyword), w -> w
                .like(Hotel::getName, keyword)
                .or()
                .like(Hotel::getAddress, keyword));
        wrapper.orderByDesc(Hotel::getRating);
        return this.page(page, wrapper);
    }

    @Override
    public Hotel getHotelDetail(Long id) {
        Hotel hotel = this.getById(id);
        if (hotel != null) {
            return hotel;
        }

        // 兜底方案：如果 hotel 表没有，去 hote_grade 表找
        HotelGrade grade = hotelGradeMapper.selectById(id);
        if (grade != null) {
            hotel = new Hotel();
            hotel.setId(grade.getId());
            hotel.setName(grade.getName());
            hotel.setCity(grade.getCity());
            hotel.setAddress(grade.getAddress());
            hotel.setRating(grade.getRating() != null ? grade.getRating() : new BigDecimal("4.5"));
            hotel.setStar(4); // 默认4星
            hotel.setPriceStart(new BigDecimal("199")); // 默认起步价
            hotel.setCoverImage(grade.getCoverImage() != null ? grade.getCoverImage() : "https://picsum.photos/800/600?random=" + id);
            hotel.setDescription(grade.getName() + "坐落于" + grade.getCity() + grade.getArea() + "，地理位置优越，环境优雅，是您出行的理想选择。");
            hotel.setStatus(1);
            return hotel;
        }

        return null;
    }

    @Override
    public List<HotelRoom> getHotelRooms(Long hotelId) {
        LambdaQueryWrapper<HotelRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelRoom::getHotelId, hotelId);
        wrapper.gt(HotelRoom::getStock, 0); // 仅查询有库存的
        List<HotelRoom> rooms = hotelRoomMapper.selectList(wrapper);
        
        // 如果没有房型，返回默认房型
        if (rooms == null || rooms.isEmpty()) {
            rooms = new ArrayList<>();
            
            HotelRoom r1 = new HotelRoom();
            r1.setId(-100L - hotelId); // 负数ID防止冲突
            r1.setHotelId(hotelId);
            r1.setName("标准大床房");
            r1.setPrice(new BigDecimal("199"));
            r1.setBedType("大床 1.8m");
            r1.setBreakfast("含单早");
            r1.setPeople(2);
            r1.setStock(10);
            r1.setImage("https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=400&h=300&fit=crop");
            rooms.add(r1);

            HotelRoom r2 = new HotelRoom();
            r2.setId(-200L - hotelId);
            r2.setHotelId(hotelId);
            r2.setName("精品双床房");
            r2.setPrice(new BigDecimal("259"));
            r2.setBedType("双床 1.2m");
            r2.setBreakfast("含双早");
            r2.setPeople(2);
            r2.setStock(5);
            r2.setImage("https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=400&h=300&fit=crop");
            rooms.add(r2);
        }
        
        return rooms;
    }
}
