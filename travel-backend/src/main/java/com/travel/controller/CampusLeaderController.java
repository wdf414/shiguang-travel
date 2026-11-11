package com.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.CampusLeader;
import com.travel.security.LoginUser;
import com.travel.service.CampusLeaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campus-leader")
public class CampusLeaderController {

    @Autowired
    private CampusLeaderService campusLeaderService;

    /**
     * 申请成为校园领队
     */
    @PostMapping("/apply")
    public Result<CampusLeader> applyLeader(@AuthenticationPrincipal LoginUser loginUser,
                                           @RequestBody CampusLeader leader) {
        CampusLeader result = campusLeaderService.applyLeader(loginUser.getUserId(), leader);
        return Result.success(result);
    }

    /**
     * 获取当前用户的领队信息
     */
    @GetMapping("/info")
    public Result<CampusLeader> getLeaderInfo(@AuthenticationPrincipal LoginUser loginUser) {
        CampusLeader leader = campusLeaderService.getLeaderByUserId(loginUser.getUserId());
        return Result.success(leader);
    }

    /**
     * 获取认证领队列表
     */
    @GetMapping("/list")
    public Result<PageResult<CampusLeader>> getLeaderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String school) {
        Page<CampusLeader> pageParam = new Page<>(page, size);
        Page<CampusLeader> result = campusLeaderService.getLeaderList(pageParam, school, 1); // 只返回已认证的
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }
}
