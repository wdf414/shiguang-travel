package com.travel.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.Result;
import com.travel.service.admin.LoginLogService;

@RestController
@RequestMapping("/api/admin/login-log")
@PreAuthorize("hasRole('ADMIN')")
public class LoginLogController {
    @Autowired
    private LoginLogService loginLogService;

    @GetMapping("/list")
    public Result<Map<String, Object>> getLoginLogs(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) String keyword) {
        return Result.success(loginLogService.getLoginLogs(page, size, keyword));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getLoginStats(@RequestParam(defaultValue = "7") int days) {
        return Result.success(loginLogService.getLoginStats(days));
    }
}

