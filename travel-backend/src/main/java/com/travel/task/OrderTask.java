package com.travel.task;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.Order;
import com.travel.entity.TourLine;
import com.travel.mapper.OrderMapper;
import com.travel.mapper.TourLineMapper;

@Component
public class OrderTask {
    private static final Logger log = LoggerFactory.getLogger(OrderTask.class);
    
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private TourLineMapper tourLineMapper;

    /**
     * 每分钟检查超时未支付订单，自动取消
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void cancelTimeoutOrders() {
        // 查询30分钟前创建且未支付的订单
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<Order> timeoutOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, "pending")
                .lt(Order::getCreateTime, timeout));
        
        for (Order order : timeoutOrders) {
            // 恢复库存
            TourLine line = tourLineMapper.selectById(order.getLineId());
            if (line != null) {
                line.setStock(line.getStock() + order.getTravelerCount());
                tourLineMapper.updateById(line);
            }
            
            // 取消订单
            order.setStatus("cancelled");
            orderMapper.updateById(order);
            log.info("订单超时自动取消: {}", order.getOrderNo());
        }
        
        if (!timeoutOrders.isEmpty()) {
            log.info("本次取消超时订单 {} 个", timeoutOrders.size());
        }
    }
}
