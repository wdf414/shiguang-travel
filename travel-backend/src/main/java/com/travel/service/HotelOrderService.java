package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.HotelOrder;

public interface HotelOrderService extends IService<HotelOrder> {
    
    /**
     * 创建酒店订单
     */
    HotelOrder createOrder(HotelOrder order);
    
    /**
     * 获取订单详情
     */
    HotelOrder getOrderDetail(String orderNo);
}
