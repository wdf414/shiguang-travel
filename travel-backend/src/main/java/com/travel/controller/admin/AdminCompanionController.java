package com.travel.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Companion;
import com.travel.service.CompanionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/companion")
public class AdminCompanionController {

    @Autowired
    private CompanionService companionService;

    /**
     * 获取所有结伴列表
     */
    @GetMapping("/list")
    public Result<PageResult<Companion>> getCompanionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<Companion> pageParam = new Page<>(page, size);
        Page<Companion> result = companionService.getAllCompanions(pageParam, keyword, status);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    /**
     * 更新结伴状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        companionService.adminUpdateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除结伴
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCompanion(@PathVariable Long id) {
        companionService.adminDeleteCompanion(id);
        return Result.success();
    }
}
