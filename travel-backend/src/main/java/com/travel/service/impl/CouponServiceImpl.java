package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.Coupon;
import com.travel.entity.UserCoupon;
import com.travel.mapper.CouponMapper;
import com.travel.mapper.UserCouponMapper;
import com.travel.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Override
    @Transactional
    public void issueCoupon(Long userId, Long couponId) {
        Coupon coupon = this.getById(couponId);
        if (coupon == null || coupon.getStatus() == 0) {
            throw new RuntimeException("Coupon not available");
        }
        if (coupon.getQuantity() <= coupon.getIssuedCount()) {
            throw new RuntimeException("Coupon out of stock");
        }
        
        // Check if verify limit per user if needed (omitted for simple logic)

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(0); // Unused
        userCouponMapper.insert(userCoupon);

        coupon.setIssuedCount(coupon.getIssuedCount() + 1);
        this.updateById(coupon);
    }

    @Override
    public List<UserCoupon> getUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(UserCoupon::getStatus, status);
        }
        queryWrapper.orderByDesc(UserCoupon::getCreateTime);
        List<UserCoupon> list = userCouponMapper.selectList(queryWrapper);
        
        if (!list.isEmpty()) {
            List<Long> couponIds = list.stream().map(UserCoupon::getCouponId).collect(java.util.stream.Collectors.toList());
            List<Coupon> coupons = this.listByIds(couponIds);
            java.util.Map<Long, Coupon> couponMap = coupons.stream().collect(java.util.stream.Collectors.toMap(Coupon::getId, c -> c));
            
            for (UserCoupon uc : list) {
                uc.setCoupon(couponMap.get(uc.getCouponId()));
            }
        }
        return list;
    }

    @Override
    public List<Coupon> getAvailableCoupons() {
        return this.list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .apply("quantity > issued_count")
                .ge(Coupon::getEndTime, LocalDateTime.now()));
    }
}
