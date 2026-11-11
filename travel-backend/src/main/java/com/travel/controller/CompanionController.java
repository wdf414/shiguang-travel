package com.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Companion;
import com.travel.security.LoginUser;
import com.travel.service.CompanionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companion")
public class CompanionController {

    @Autowired
    private CompanionService companionService;

    @GetMapping("/list")
    public Result<PageResult<Companion>> getCompanionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String school,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String grade) {
        Page<Companion> pageParam = new Page<>(page, size);
        Page<Companion> result = companionService.getCompanionList(pageParam, keyword, status, school, major, grade);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<Companion> getCompanionDetail(@PathVariable Long id) {
        Companion companion = companionService.getCompanionDetail(id);
        return Result.success(companion);
    }

    @PostMapping
    public Result<Companion> createCompanion(@AuthenticationPrincipal LoginUser loginUser,
                                           @RequestBody Companion companion) {
        Companion created = companionService.createCompanion(loginUser.getUserId(), companion);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Void> updateCompanion(@AuthenticationPrincipal LoginUser loginUser,
                                      @PathVariable Long id,
                                      @RequestBody Companion companion) {
        companionService.updateCompanion(loginUser.getUserId(), id, companion);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCompanion(@AuthenticationPrincipal LoginUser loginUser,
                                      @PathVariable Long id) {
        companionService.deleteCompanion(loginUser.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/join")
    public Result<Void> joinCompanion(@AuthenticationPrincipal LoginUser loginUser,
                                    @PathVariable Long id) {
        companionService.joinCompanion(loginUser.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/quit")
    public Result<Void> quitCompanion(@AuthenticationPrincipal LoginUser loginUser,
                                    @PathVariable Long id) {
        companionService.quitCompanion(loginUser.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/joined")
    public Result<PageResult<Companion>> getUserJoinedCompanions(@AuthenticationPrincipal LoginUser loginUser,
                                                                @RequestParam(defaultValue = "1") Integer page,
                                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Companion> pageParam = new Page<>(page, size);
        Page<Companion> result = companionService.getUserJoinedCompanions(loginUser.getUserId(), pageParam);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/published")
    public Result<PageResult<Companion>> getUserPublishedCompanions(@AuthenticationPrincipal LoginUser loginUser,
                                                                   @RequestParam(defaultValue = "1") Integer page,
                                                                   @RequestParam(defaultValue = "10") Integer size) {
        Page<Companion> pageParam = new Page<>(page, size);
        Page<Companion> result = companionService.getUserPublishedCompanions(loginUser.getUserId(), pageParam);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/{id}/group-chat")
    public Result<Companion> getGroupChat(@PathVariable Long id) {
        Companion companion = companionService.getGroupChat(id);
        return Result.success(companion);
    }

    @GetMapping("/{id}/members")
    public Result<java.util.List<com.travel.dto.CompanionMemberDTO>> getCompanionMembers(@PathVariable Long id) {
        return Result.success(companionService.getCompanionMemberDetails(id));
    }
}