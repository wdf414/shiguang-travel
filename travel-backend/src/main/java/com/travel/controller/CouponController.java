package com.travel.controller;

import com.travel.common.Result;
import com.travel.entity.Coupon;
import com.travel.entity.UserCoupon;
import com.travel.security.LoginUser;
import com.travel.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    // Get available coupons for user to claim
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons() {
        return Result.success(couponService.getAvailableCoupons());
    }

    // Claim coupon
    @PostMapping("/claim/{id}")
    public Result<?> claimCoupon(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.error(401, "未登录");
        }
        Long userId = loginUser.getUserId();
        try {
            couponService.issueCoupon(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // Get my coupons
    @GetMapping("/my")
    public Result<List<UserCoupon>> getMyCoupons(@RequestParam(required = false) Integer status, @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null) {
            return Result.error(401, "未登录");
        }
        Long userId = loginUser.getUserId();
        return Result.success(couponService.getUserCoupons(userId, status));
    }
}
