package com.travel.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.User;
import com.travel.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/list")
    public Result<PageResult<User>> getUserList(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return Result.success(adminUserService.getUserList(keyword, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        return Result.success(adminUserService.getUserDetail(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        adminUserService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id,
                                       @RequestParam String role) {
        adminUserService.updateUserRole(id, role);
        return Result.success();
    }

    @PutMapping("/{id}/student")
    public Result<Void> updateUserStudentStatus(@PathVariable Long id,
                                                @RequestParam Integer isStudent) {
        adminUserService.updateUserStudentStatus(id, isStudent);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> updateUserPassword(@PathVariable Long id,
                                           @RequestBody Map<String, String> body) {
        adminUserService.updateUserPassword(id, body.get("password"));
        return Result.success();
    }
}
