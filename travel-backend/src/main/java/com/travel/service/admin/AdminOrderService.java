package com.travel.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.entity.Order;
import com.travel.entity.TourLine;
import com.travel.exception.BusinessException;
import com.travel.mapper.OrderMapper;
import com.travel.mapper.TourLineMapper;

@Service
public class AdminOrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TourLineMapper tourLineMapper;

    public PageResult<Order> getOrderList(String status, String keyword, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword)
                    .or().like(Order::getLineName, keyword)
                    .or().like(Order::getContactName, keyword));
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    public Order getOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    public void updateStatus(Long id, String status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }

    @Transactional
    public void refundOrder(Long id, String reason) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"paid".equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许退款");
        }
        
        // 恢复库存
        TourLine line = tourLineMapper.selectById(order.getLineId());
        if (line != null) {
            line.setStock(line.getStock() + order.getTravelerCount());
            line.setSales(line.getSales() - order.getTravelerCount());
            tourLineMapper.updateById(line);
        }
        
        // 更新订单状态
        order.setStatus("refunded");
        order.setRemark(order.getRemark() + " [退款原因: " + reason + "]");
        orderMapper.updateById(order);
    }
}
