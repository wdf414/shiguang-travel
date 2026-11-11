package com.travel.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.CampusLeader;
import com.travel.entity.LeaderCommission;
import com.travel.service.CampusLeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/campus-leader")
public class AdminCampusLeaderController {

    @Autowired
    private CampusLeaderService campusLeaderService;

    /**
     * 获取领队列表
     */
    @GetMapping("/list")
    public Result<PageResult<CampusLeader>> getLeaderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) Integer status) {
        Page<CampusLeader> pageParam = new Page<>(page, size);
        Page<CampusLeader> result = campusLeaderService.getLeaderList(pageParam, school, status);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /**
     * 审核领队申请
     */
    @PutMapping("/{id}/audit")
    public Result<Void> auditLeader(@PathVariable Long id,
                                    @RequestParam Integer status) {
        campusLeaderService.auditLeader(id, status);
        return Result.success();
    }

    /**
     * 设置佣金比例
     */
    @PutMapping("/{id}/commission-rate")
    public Result<Void> setCommissionRate(@PathVariable Long id,
                                         @RequestParam BigDecimal rate) {
        campusLeaderService.setCommissionRate(id, rate);
        return Result.success();
    }

    /**
     * 查看领队佣金明细
     */
    @GetMapping("/{id}/commissions")
    public Result<PageResult<LeaderCommission>> getCommissions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<LeaderCommission> pageParam = new Page<>(page, size);
        Page<LeaderCommission> result = campusLeaderService.getCommissions(id, pageParam);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /**
     * 删除领队
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteLeader(@PathVariable Long id) {
        campusLeaderService.deleteLeader(id);
        return Result.success();
    }
}
