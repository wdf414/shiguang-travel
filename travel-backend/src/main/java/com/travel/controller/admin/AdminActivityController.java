package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.Result;
import com.travel.entity.Activity;
import com.travel.service.ActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activity")
public class AdminActivityController {

    private static final Logger log = LoggerFactory.getLogger(AdminActivityController.class);

    @Autowired
    private ActivityService activityService;

    @GetMapping("/list")
    public Result<Page<Activity>> getList(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          String title) {
        try {
            Page<Activity> page = new Page<>(current, size);
            LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
            if (title != null && !title.isEmpty()) {
                wrapper.like(Activity::getTitle, title);
            }
            wrapper.orderByDesc(Activity::getCreateTime);
            return Result.success(activityService.page(page, wrapper));
        } catch (Exception e) {
            log.error("获取活动列表失败", e);
            return Result.error("获取活动列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Activity activity) {
        try {
            log.info("创建活动: {}", activity);
            activityService.save(activity);
            return Result.success();
        } catch (Exception e) {
            log.error("创建活动失败", e);
            return Result.error("创建活动失败: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody Activity activity) {
        try {
            log.info("更新活动: {}", activity);
            activityService.updateById(activity);
            return Result.success();
        } catch (Exception e) {
            log.error("更新活动失败", e);
            return Result.error("更新活动失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            log.info("删除活动: {}", id);
            activityService.removeById(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除活动失败", e);
            return Result.error("删除活动失败: " + e.getMessage());
        }
    }
}
