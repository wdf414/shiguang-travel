package com.travel.controller;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.dto.OrderCreateDTO;
import com.travel.entity.Order;
import com.travel.security.LoginUser;
import com.travel.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@AuthenticationPrincipal LoginUser loginUser,
                                                   @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(loginUser.getUserId(), dto));
    }

    @GetMapping("/list")
    public Result<PageResult<Order>> getOrderList(@AuthenticationPrincipal LoginUser loginUser,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.getOrderList(loginUser.getUserId(), status, page, size));
    }

    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@AuthenticationPrincipal LoginUser loginUser,
                                        @PathVariable String orderNo) {
        return Result.success(orderService.getOrderDetail(loginUser.getUserId(), orderNo));
    }

    @PutMapping("/{orderNo}/cancel")
    public Result<Void> cancelOrder(@AuthenticationPrincipal LoginUser loginUser,
                                    @PathVariable String orderNo) {
        orderService.cancelOrder(loginUser.getUserId(), orderNo);
        return Result.success();
    }

    @PostMapping("/{orderNo}/pay")
    public Result<Void> payOrder(@AuthenticationPrincipal LoginUser loginUser,
                                 @PathVariable String orderNo,
                                 @RequestBody Map<String, String> body) {
        orderService.payOrder(loginUser.getUserId(), orderNo, body.get("payType"));
        return Result.success();
    }

    @GetMapping("/{orderNo}/pay-status")
    public Result<String> getPayStatus(@AuthenticationPrincipal LoginUser loginUser,
                                       @PathVariable String orderNo) {
        return Result.success(orderService.getPayStatus(loginUser.getUserId(), orderNo));
    }
}
