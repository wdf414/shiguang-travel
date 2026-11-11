package com.travel.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.dto.LineQueryDTO;
import com.travel.entity.TourLine;
import com.travel.security.LoginUser;
import com.travel.service.admin.AdminLineService;

@RestController
@RequestMapping("/api/admin/line")
@PreAuthorize("hasRole('ADMIN')")
public class AdminLineController {
    @Autowired
    private AdminLineService adminLineService;

    @GetMapping("/list")
    public Result<PageResult<TourLine>> getLineList(LineQueryDTO dto) {
        return Result.success(adminLineService.getLineList(dto));
    }

    @GetMapping("/{id}")
    public Result<TourLine> getLineDetail(@PathVariable Long id) {
        return Result.success(adminLineService.getLineDetail(id));
    }

    @PostMapping
    public Result<Void> createLine(@AuthenticationPrincipal LoginUser loginUser,
                                   @RequestBody TourLine line) {
        adminLineService.createLine(line);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateLine(@PathVariable Long id,
                                   @RequestBody TourLine line) {
        line.setId(id);
        adminLineService.updateLine(line);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        adminLineService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteLine(@PathVariable Long id) {
        adminLineService.deleteLine(id);
        return Result.success();
    }
}
