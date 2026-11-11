package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.dto.OrderCreateDTO;
import com.travel.entity.Order;
import com.travel.entity.TourLine;
import com.travel.entity.Traveler;
import com.travel.exception.BusinessException;
import com.travel.mapper.OrderMapper;
import com.travel.mapper.TourLineMapper;
import com.travel.mapper.TravelerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TourLineMapper tourLineMapper;
    @Autowired
    private TravelerMapper travelerMapper;
    @Autowired
    private com.travel.mapper.UserCouponMapper userCouponMapper;
    @Autowired
    private com.travel.mapper.CouponMapper couponMapper;

    @Transactional
    public Map<String, Object> createOrder(Long userId, OrderCreateDTO dto) {
        TourLine line = tourLineMapper.selectById(dto.getLineId());
        if (line == null || line.getStatus() != 1) {
            throw new BusinessException("线路不存在");
        }
        
        int travelerCount = dto.getTravelers().size();
        if (line.getStock() < travelerCount) {
            throw new BusinessException("库存不足");
        }
        
        // 创建订单
        Order order = new Order();
        order.setOrderNo("T" + System.currentTimeMillis());
        order.setUserId(userId);
        order.setLineId(line.getId());
        order.setLineName(line.getTitle());
        order.setLineCover(line.getCoverImage());
        order.setDepartureDate(LocalDate.parse(dto.getDepartureDate()));
        order.setTravelerCount(travelerCount);
        order.setTotalPrice(line.getPrice().multiply(BigDecimal.valueOf(travelerCount)));
        order.setContactName(dto.getContactName());
        order.setContactPhone(dto.getContactPhone());
        order.setRemark(dto.getRemark());
        order.setPayType(dto.getPayType());
        order.setStatus("pending");
        orderMapper.insert(order);
        
        // 保存出行人
        for (OrderCreateDTO.TravelerDTO t : dto.getTravelers()) {
            Traveler traveler = new Traveler();
            traveler.setOrderId(order.getId());
            traveler.setName(t.getName());
            traveler.setIdCard(t.getIdCard());
            traveler.setPhone(t.getPhone());
            travelerMapper.insert(traveler);
        }
        
        // 扣减库存
        line.setStock(line.getStock() - travelerCount);
        tourLineMapper.updateById(line);

        // 处理优惠券
        if (dto.getCouponId() != null) {
            com.travel.entity.UserCoupon userCoupon = userCouponMapper.selectById(dto.getCouponId());
            if (userCoupon == null || !userCoupon.getUserId().equals(userId) || userCoupon.getStatus() != 0) {
                throw new BusinessException("优惠券无效或不可用");
            }
            
            com.travel.entity.Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            if (coupon == null || coupon.getStatus() == 0 || LocalDateTime.now().isAfter(coupon.getEndTime())) {
                throw new BusinessException("优惠券已过期或下架");
            }
            
            if (order.getTotalPrice().compareTo(coupon.getThresholdAmount()) < 0) {
                throw new BusinessException("未满足优惠券使用门槛");
            }
            
            BigDecimal discount = coupon.getDiscountAmount();
            // 确保不出现负数金额
            if (order.getTotalPrice().compareTo(discount) < 0) {
               order.setTotalPrice(BigDecimal.ZERO);
            } else {
               order.setTotalPrice(order.getTotalPrice().subtract(discount));
            }
            
            // 更新优惠券状态
            userCoupon.setStatus(1);
            userCoupon.setUseTime(LocalDateTime.now());
            userCoupon.setOrderId(order.getId());
            userCouponMapper.updateById(userCoupon);
            
            // 记录优惠信息到订单备注
            String couponInfo = " [使用优惠券: " + coupon.getName() + ", 减免: " + discount + "]";
            order.setRemark(order.getRemark() == null ? couponInfo : order.getRemark() + couponInfo);
            // 需要更新订单价格
            orderMapper.updateById(order);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", order.getOrderNo());
        result.put("totalPrice", order.getTotalPrice());
        return result;
    }

    public PageResult<Order> getOrderList(Long userId, String status, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public Order getOrderDetail(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Transactional
    public void cancelOrder(Long userId, String orderNo) {
        Order order = getOrderDetail(userId, orderNo);
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许取消");
        }
        
        // 恢复库存
        TourLine line = tourLineMapper.selectById(order.getLineId());
        if (line != null) {
            line.setStock(line.getStock() + order.getTravelerCount());
            tourLineMapper.updateById(line);
        }
        
        order.setStatus("cancelled");
        orderMapper.updateById(order);
    }

    @Transactional
    public void payOrder(Long userId, String orderNo, String payType) {
        Order order = getOrderDetail(userId, orderNo);
        if (!"pending".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        
        // 模拟支付成功
        order.setStatus("paid");
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        // 增加销量
        TourLine line = tourLineMapper.selectById(order.getLineId());
        if (line != null) {
            line.setSales(line.getSales() + order.getTravelerCount());
            tourLineMapper.updateById(line);
        }
    }

    public String getPayStatus(Long userId, String orderNo) {
        Order order = getOrderDetail(userId, orderNo);
        return order.getStatus();
    }
}
