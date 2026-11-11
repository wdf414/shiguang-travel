package com.travel.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Order;
import com.travel.service.admin.AdminOrderService;

@RestController
@RequestMapping("/api/admin/order")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {
    @Autowired
    private AdminOrderService adminOrderService;

    @GetMapping("/list")
    public Result<PageResult<Order>> getOrderList(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminOrderService.getOrderList(status, keyword, page, size));
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        return Result.success(adminOrderService.getOrderDetail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        adminOrderService.updateStatus(id, body.get("status"));
        return Result.success();
    }

    @PutMapping("/{id}/refund")
    public Result<Void> refundOrder(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        adminOrderService.refundOrder(id, body.get("reason"));
        return Result.success();
    }
}
