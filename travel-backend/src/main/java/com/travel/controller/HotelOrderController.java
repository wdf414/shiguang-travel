package com.travel.controller;

import com.travel.common.Result;
import com.travel.entity.HotelOrder;
import com.travel.security.LoginUser;
import com.travel.service.HotelOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotel-orders")
public class HotelOrderController {

    @Autowired
    private HotelOrderService hotelOrderService;

    @PostMapping
    public Result<HotelOrder> createOrder(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody HotelOrder order) {
        order.setUserId(loginUser.getUserId());
        try {
            HotelOrder createdOrder = hotelOrderService.createOrder(order);
            return Result.success(createdOrder);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{orderNo}")
    public Result<HotelOrder> getOrderDetail(@PathVariable String orderNo) {
        return Result.success(hotelOrderService.getOrderDetail(orderNo));
    }
}
