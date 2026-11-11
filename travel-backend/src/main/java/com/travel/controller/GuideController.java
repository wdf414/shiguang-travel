package com.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Guide;
import com.travel.security.LoginUser;
import com.travel.service.GuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/guide")
public class GuideController {

    @Autowired
    private GuideService guideService;

    @GetMapping("/list")
    public Result<PageResult<Guide>> getGuideList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<Guide> pageParam = new Page<>(page, size);
        Page<Guide> result = guideService.getGuideList(pageParam, keyword, status);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<Guide> getGuideDetail(@PathVariable Long id) {
        Guide guide = guideService.getGuideDetail(id);
        guideService.increaseViewCount(id); // 增加浏览数
        return Result.success(guide);
    }

    @PostMapping
    public Result<Guide> createGuide(@AuthenticationPrincipal LoginUser loginUser,
                                   @RequestBody Guide guide) {
        Guide created = guideService.createGuide(loginUser.getUserId(), guide);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<Void> updateGuide(@AuthenticationPrincipal LoginUser loginUser,
                                  @PathVariable Long id,
                                  @RequestBody Guide guide) {
        guideService.updateGuide(loginUser.getUserId(), id, guide);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGuide(@AuthenticationPrincipal LoginUser loginUser,
                                  @PathVariable Long id) {
        guideService.deleteGuide(loginUser.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publishGuide(@AuthenticationPrincipal LoginUser loginUser,
                                   @PathVariable Long id) {
        guideService.publishGuide(loginUser.getUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeGuide(@AuthenticationPrincipal LoginUser loginUser,
                                @PathVariable Long id) {
        guideService.toggleLike(loginUser.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<Guide>> getMyGuides(@AuthenticationPrincipal LoginUser loginUser,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Guide> pageParam = new Page<>(page, size);
        Page<Guide> result = guideService.getUserGuides(loginUser.getUserId(), pageParam);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, size));
    }
}