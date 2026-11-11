package com.travel.service.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.entity.TourLine;
import com.travel.mapper.TourLineMapper;

@Service
public class PriceManageService {
    @Autowired
    private TourLineMapper tourLineMapper;

    // 批量调价
    @Transactional
    public void batchAdjustPrice(List<Long> lineIds, String adjustType, BigDecimal adjustValue) {
        List<TourLine> lines = tourLineMapper.selectBatchIds(lineIds);
        
        for (TourLine line : lines) {
            BigDecimal newPrice = line.getPrice();
            
            if ("fixed".equals(adjustType)) {
                // 固定价格
                newPrice = adjustValue;
            } else if ("add".equals(adjustType)) {
                // 增加金额
                newPrice = line.getPrice().add(adjustValue);
            } else if ("subtract".equals(adjustType)) {
                // 减少金额
                newPrice = line.getPrice().subtract(adjustValue);
            } else if ("multiply".equals(adjustType)) {
                // 按比例增加（adjustValue是倍数，如1.2表示增加20%）
                newPrice = line.getPrice().multiply(adjustValue);
            } else if ("percent".equals(adjustType)) {
                // 按百分比调整（adjustValue是百分比，如10表示增加10%）
                BigDecimal percent = adjustValue.divide(BigDecimal.valueOf(100));
                newPrice = line.getPrice().multiply(BigDecimal.ONE.add(percent));
            }
            
            // 确保价格不为负数
            if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
                newPrice = BigDecimal.ZERO;
            }
            
            line.setPrice(newPrice);
            tourLineMapper.updateById(line);
        }
    }

    // 设置旺季/淡季价格
    @Transactional
    public void setSeasonPrice(Long lineId, String season, BigDecimal price) {
        TourLine line = tourLineMapper.selectById(lineId);
        if (line == null) {
            throw new RuntimeException("线路不存在");
        }
        
        // 这里简化处理，实际应该存储到单独的表中
        // 暂时直接更新价格
        line.setPrice(price);
        tourLineMapper.updateById(line);
    }

    // 获取价格调整历史（简化版）
    public Map<String, Object> getPriceHistory(Long lineId) {
        Map<String, Object> result = new HashMap<>();
        TourLine line = tourLineMapper.selectById(lineId);
        if (line != null) {
            result.put("currentPrice", line.getPrice());
            result.put("lastUpdate", line.getUpdateTime());
        }
        return result;
    }

    // 动态价格调整（根据库存、销量等）
    @Transactional
    public void dynamicPriceAdjust(Long lineId, Map<String, Object> factors) {
        TourLine line = tourLineMapper.selectById(lineId);
        if (line == null) {
            throw new RuntimeException("线路不存在");
        }
        
        BigDecimal basePrice = line.getPrice();
        BigDecimal adjustment = BigDecimal.ZERO;
        
        // 根据库存调整
        if (factors.containsKey("stockFactor")) {
            Integer stock = line.getStock() != null ? line.getStock() : 0;
            if (stock < 10) {
                // 库存紧张，价格上涨10%
                adjustment = adjustment.add(basePrice.multiply(BigDecimal.valueOf(0.1)));
            }
        }
        
        // 根据销量调整
        if (factors.containsKey("salesFactor")) {
            Integer sales = line.getSales() != null ? line.getSales() : 0;
            if (sales > 100) {
                // 销量高，价格上涨5%
                adjustment = adjustment.add(basePrice.multiply(BigDecimal.valueOf(0.05)));
            }
        }
        
        // 根据季节调整
        if (factors.containsKey("seasonFactor")) {
            int month = LocalDate.now().getMonthValue();
            if (month >= 5 && month <= 10) {
                // 旺季（5-10月），价格上涨15%
                adjustment = adjustment.add(basePrice.multiply(BigDecimal.valueOf(0.15)));
            }
        }
        
        BigDecimal newPrice = basePrice.add(adjustment);
        line.setPrice(newPrice);
        tourLineMapper.updateById(line);
    }
}
