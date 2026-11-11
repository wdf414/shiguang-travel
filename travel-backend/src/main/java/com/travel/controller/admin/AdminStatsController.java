package com.travel.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.common.Result;
import com.travel.service.admin.AdminStatsService;

@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {
    @Autowired
    private AdminStatsService adminStatsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(adminStatsService.getOverview());
    }

    @GetMapping("/sales")
    public Result<Map<String, Object>> getSalesTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(adminStatsService.getSalesTrend(days));
    }

    @GetMapping("/users")
    public Result<Map<String, Object>> getUserTrend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(adminStatsService.getUserTrend(days));
    }

    @GetMapping("/login-hours")
    public Result<Map<String, Object>> getLoginHourStats() {
        return Result.success(adminStatsService.getLoginDistribution());
    }

    @GetMapping("/hot-lines")
    public Result<Map<String, Object>> getHotLinesRanking(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(adminStatsService.getHotLinesRanking(limit));
    }

    @GetMapping("/purchase-behavior")
    public Result<Map<String, Object>> getUserPurchaseBehavior() {
        return Result.success(adminStatsService.getUserPurchaseBehavior());
    }

    @GetMapping("/user-activity")
    public Result<Map<String, Object>> getUserActivity(@RequestParam(defaultValue = "30") int days) {
        return Result.success(adminStatsService.getUserActivity(days));
    }

    @GetMapping("/user-profile")
    public Result<Map<String, Object>> getUserProfile() {
        return Result.success(adminStatsService.getUserProfile());
    }

    @GetMapping("/page-visits")
    public Result<Map<String, Object>> getPageVisitStats(@RequestParam(defaultValue = "30") int days) {
        return Result.success(adminStatsService.getPageVisitStats(days));
    }

    @GetMapping("/user-source")
    public Result<Map<String, Object>> getUserSource() {
        return Result.success(adminStatsService.getUserSource());
    }

    @GetMapping("/conversion-rate")
    public Result<Map<String, Object>> getConversionRate(@RequestParam(defaultValue = "30") int days) {
        return Result.success(adminStatsService.getConversionRate(days));
    }

    @GetMapping("/student-analysis")
    public Result<Map<String, Object>> getStudentAnalysis() {
        return Result.success(adminStatsService.getStudentAnalysis());
    }

    @GetMapping("/student-destinations")
    public Result<Map<String, Object>> getStudentHotDestinations(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(adminStatsService.getStudentHotDestinations(limit));
    }

    @GetMapping("/group-travel-trend")
    public Result<Map<String, Object>> getGroupTravelTrend(@RequestParam(defaultValue = "30") int days) {
        return Result.success(adminStatsService.getGroupTravelTrend(days));
    }
}
