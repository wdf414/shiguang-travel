package com.travel.controller.admin;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.ScenicSpot;
import com.travel.service.admin.AdminScenicSpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/scenic-spot")
@PreAuthorize("hasRole('ADMIN')")
public class AdminScenicSpotController {
    @Autowired
    private AdminScenicSpotService adminScenicSpotService;

    @GetMapping("/page")
    public Result<PageResult<ScenicSpot>> getPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city) {
        return Result.success(adminScenicSpotService.getPage(page, size, keyword, city));
    }

    @GetMapping("/{id}")
    public Result<ScenicSpot> getDetail(@PathVariable Long id) {
        return Result.success(adminScenicSpotService.getDetail(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody ScenicSpot spot) {
        adminScenicSpotService.create(spot);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody ScenicSpot spot) {
        adminScenicSpotService.update(spot);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminScenicSpotService.delete(id);
        return Result.success();
    }
}
