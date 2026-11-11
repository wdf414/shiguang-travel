package com.travel.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.travel.common.Result;
import com.travel.service.admin.PriceManageService;

@RestController
@RequestMapping("/api/admin/price")
@PreAuthorize("hasRole('ADMIN')")
public class PriceManageController {
    @Autowired
    private PriceManageService priceManageService;

    // 批量调价
    @PostMapping("/batch-adjust")
    public Result<Void> batchAdjustPrice(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> lineIds = (List<Long>) params.get("lineIds");
        String adjustType = (String) params.get("adjustType");
        BigDecimal adjustValue = new BigDecimal(params.get("adjustValue").toString());
        
        priceManageService.batchAdjustPrice(lineIds, adjustType, adjustValue);
        return Result.success();
    }

    // 设置旺季/淡季价格
    @PostMapping("/season-price")
    public Result<Void> setSeasonPrice(@RequestBody Map<String, Object> params) {
        Long lineId = Long.valueOf(params.get("lineId").toString());
        String season = (String) params.get("season");
        BigDecimal price = new BigDecimal(params.get("price").toString());
        
        priceManageService.setSeasonPrice(lineId, season, price);
        return Result.success();
    }

    // 动态价格调整
    @PostMapping("/dynamic-adjust/{lineId}")
    public Result<Void> dynamicPriceAdjust(@PathVariable Long lineId, @RequestBody Map<String, Object> factors) {
        priceManageService.dynamicPriceAdjust(lineId, factors);
        return Result.success();
    }

    // 获取价格历史
    @GetMapping("/history/{lineId}")
    public Result<Map<String, Object>> getPriceHistory(@PathVariable Long lineId) {
        return Result.success(priceManageService.getPriceHistory(lineId));
    }
}
