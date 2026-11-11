package com.travel.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.security.LoginUser;
import com.travel.service.UserRouteSpotService;

@RestController
@RequestMapping("/api/user/route-spot")
public class UserRouteSpotController {
    @Autowired
    private UserRouteSpotService userRouteSpotService;

    @PostMapping("/add")
    public Result<Void> add(@AuthenticationPrincipal LoginUser loginUser,
                            @RequestBody Map<String, Long> body) {
        Long scenicSpotId = body.get("scenicSpotId");
        userRouteSpotService.addSpot(loginUser.getUserId(), scenicSpotId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<Map<String, Object>>> list(@AuthenticationPrincipal LoginUser loginUser,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return Result.success(userRouteSpotService.list(loginUser.getUserId(), page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal LoginUser loginUser,
                               @PathVariable Long id) {
        userRouteSpotService.remove(loginUser.getUserId(), id);
        return Result.success();
    }
}

