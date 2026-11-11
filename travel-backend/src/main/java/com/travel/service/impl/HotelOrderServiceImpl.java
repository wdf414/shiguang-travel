package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.Hotel;
import com.travel.entity.HotelOrder;
import com.travel.entity.HotelRoom;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.HotelOrderMapper;
import com.travel.mapper.HotelRoomMapper;
import com.travel.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class HotelOrderServiceImpl extends ServiceImpl<HotelOrderMapper, HotelOrder> implements HotelOrderService {

    @Autowired
    private HotelMapper hotelMapper;
    
    @Autowired
    private HotelRoomMapper hotelRoomMapper;

    @Override
    @Transactional
    public HotelOrder createOrder(HotelOrder order) {
        // 1. 校验酒店和房型
        Hotel hotel = hotelMapper.selectById(order.getHotelId());
        if (hotel == null) {
            // 如果按ID查不到酒店，尝试使用传入的名称（兼容虚拟酒店数据）
            if (order.getHotelName() == null) {
                throw new RuntimeException("酒店不存在");
            }
            // 使用虚拟酒店信息，不阻断流程
        }
        
        HotelRoom room = hotelRoomMapper.selectById(order.getRoomId());
        // 如果房型ID不存在或者是负数（虚拟房型），则跳过库存检查
        boolean isVirtualRoom = room == null || order.getRoomId() < 0;
        
        if (!isVirtualRoom) {
            // 2. 扣减库存 (简化处理，不锁表)
            if (room.getStock() < order.getRoomCount()) {
                throw new RuntimeException("库存不足");
            }
            room.setStock(room.getStock() - order.getRoomCount());
            hotelRoomMapper.updateById(room);
        }
        
        // 3. 计算价格
        long days = ChronoUnit.DAYS.between(order.getCheckIn(), order.getCheckOut());
        if (days <= 0) days = 1;
        
        if (!isVirtualRoom) {
            BigDecimal price = room.getPrice().multiply(new BigDecimal(days)).multiply(new BigDecimal(order.getRoomCount()));
            order.setTotalPrice(price);
            order.setRoomName(room.getName());
        } else {
            // 虚拟房型：如果前端传了总价则使用，否则需要处理
            if (order.getTotalPrice() == null) {
                throw new RuntimeException("价格计算错误");
            }
            // 确保房型名称存在
            if (order.getRoomName() == null) {
                order.setRoomName("标准间");
            }
        }
        
        order.setNightCount((int)days);
        
        // 4. 填充信息
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setStatus("paid"); // 简化支付流程，直接设为已支付
        if (hotel != null) {
            order.setHotelName(hotel.getName());
        }
        // 如果 hotel 为 null，order.hotelName 应该已经在 controller 或前端设置好了
        
        this.save(order);
        return order;
    }

    @Override
    public HotelOrder getOrderDetail(String orderNo) {
        LambdaQueryWrapper<HotelOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelOrder::getOrderNo, orderNo);
        return this.getOne(wrapper);
    }
}
