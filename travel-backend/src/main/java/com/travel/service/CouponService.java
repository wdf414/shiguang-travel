package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.Coupon;
import com.travel.entity.UserCoupon;
import java.util.List;

public interface CouponService extends IService<Coupon> {
    void issueCoupon(Long userId, Long couponId);
    List<UserCoupon> getUserCoupons(Long userId, Integer status);
    List<Coupon> getAvailableCoupons();
}
