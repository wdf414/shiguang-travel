package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.Result;
import com.travel.entity.Coupon;
import com.travel.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/coupon")
public class AdminCouponController {

    private static final Logger log = LoggerFactory.getLogger(AdminCouponController.class);

    @Autowired
    private CouponService couponService;

    @GetMapping("/list")
    public Result<Page<Coupon>> getList(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        String name) {
        try {
            Page<Coupon> page = new Page<>(current, size);
            LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
            if (name != null && !name.isEmpty()) {
                wrapper.like(Coupon::getName, name);
            }
            wrapper.orderByDesc(Coupon::getCreateTime);
            return Result.success(couponService.page(page, wrapper));
        } catch (Exception e) {
            log.error("获取优惠券列表失败", e);
            return Result.error("获取优惠券列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Coupon coupon) {
        try {
            log.info("创建优惠券: {}", coupon);
            couponService.save(coupon);
            return Result.success();
        } catch (Exception e) {
            log.error("创建优惠券失败", e);
            return Result.error("创建优惠券失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Coupon coupon) {
        try {
            log.info("更新优惠券: {}", coupon);
            couponService.updateById(coupon);
            return Result.success();
        } catch (Exception e) {
            log.error("更新优惠券失败", e);
            return Result.error("更新优惠券失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            log.info("删除优惠券: {}", id);
            couponService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除优惠券失败", e);
            return Result.error("删除优惠券失败: " + e.getMessage());
        }
    }
}
