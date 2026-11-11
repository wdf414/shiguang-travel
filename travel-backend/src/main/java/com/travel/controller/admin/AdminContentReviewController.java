package com.travel.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.Result;
import com.travel.entity.*;
import com.travel.service.*;
import com.travel.vo.ContentReviewVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/review")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentReviewController {

    private static final Logger log = LoggerFactory.getLogger(AdminContentReviewController.class);

    @Autowired
    private ContentReviewService contentReviewService;
    
    @Autowired
    private TravelNoteService travelNoteService;
    
    @Autowired
    private CompanionService companionService;
    
    @Autowired
    private TravelNoteCommentService commentService;
    
    @Autowired
    private UserService userService;

    /**
     * 获取待审核内容列表
     */
    @GetMapping("/pending")
    public Result<?> getPendingContent(
            @RequestParam String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Page<?> resultPage;
        
        switch (type) {
            case "travel_note":
                resultPage = travelNoteService.page(
                    new Page<>(page, size),
                    new LambdaQueryWrapper<TravelNote>()
                        .eq(TravelNote::getReviewStatus, 0)
                        .orderByDesc(TravelNote::getCreateTime)
                );
                break;
            case "companion":
                resultPage = companionService.page(
                    new Page<>(page, size),
                    new LambdaQueryWrapper<Companion>()
                        .eq(Companion::getReviewStatus, 0)
                        .orderByDesc(Companion::getCreateTime)
                );
                break;
            case "comment":
                resultPage = commentService.page(
                    new Page<>(page, size),
                    new LambdaQueryWrapper<TravelNoteComment>()
                        .eq(TravelNoteComment::getReviewStatus, 0)
                        .orderByDesc(TravelNoteComment::getCreateTime)
                );
                break;
            default:
                return Result.error("未知的内容类型");
        }
        
        return Result.success(resultPage);
    }

    /**
     * 审核内容
     */
    @PostMapping("/audit")
    public Result<?> auditContent(@RequestBody ContentReviewVO reviewVO) {
        try {
            // 获取当前登录管理员信息（这里简化处理，实际应从SecurityContext获取）
            Long reviewerId = 1L; // TODO: 从登录信息获取
            String reviewerName = "管理员"; // TODO: 从登录信息获取
            
            contentReviewService.manualReview(
                reviewVO.getContentType(),
                reviewVO.getContentId(),
                reviewVO.getNewStatus(),
                reviewVO.getRejectReason(),
                reviewerId,
                reviewerName
            );
            
            return Result.success("审核完成");
        } catch (Exception e) {
            log.error("审核失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    /**
     * 批量审核
     */
    @PostMapping("/batch-audit")
    public Result<?> batchAudit(@RequestBody ContentReviewVO batchVO) {
        try {
            Long reviewerId = 1L;
            String reviewerName = "管理员";
            
            for (Long contentId : batchVO.getContentIds()) {
                contentReviewService.manualReview(
                    batchVO.getContentType(),
                    contentId,
                    batchVO.getNewStatus(),
                    batchVO.getRejectReason(),
                    reviewerId,
                    reviewerName
                );
            }
            
            return Result.success("批量审核完成");
        } catch (Exception e) {
            log.error("批量审核失败", e);
            return Result.error("批量审核失败：" + e.getMessage());
        }
    }

    /**
     * 获取审核记录
     */
    @GetMapping("/records")
    public Result<?> getReviewRecords(
            @RequestParam String contentType,
            @RequestParam Long contentId) {
        
        List<ReviewRecord> records = contentReviewService.getReviewRecords(contentType, contentId);
        return Result.success(records);
    }

    /**
     * 检测敏感词
     */
    @PostMapping("/check-sensitive")
    public Result<?> checkSensitiveWords(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        if (content == null || content.isEmpty()) {
            return Result.success(Map.of("hasSensitive", false, "words", List.of()));
        }
        
        List<String> words = contentReviewService.checkSensitiveWords(content);
        Map<String, Object> result = new HashMap<>();
        result.put("hasSensitive", !words.isEmpty());
        result.put("words", words);
        
        return Result.success(result);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 待审核游记数量 (使用 review_status 字段，0-待审核)
        long pendingNotes = travelNoteService.count(
            new LambdaQueryWrapper<TravelNote>().eq(TravelNote::getReviewStatus, 0)
        );
        
        // 待审核结伴数量
        long pendingCompanions = companionService.count(
            new LambdaQueryWrapper<Companion>().eq(Companion::getReviewStatus, 0)
        );
        
        // 待审核评论数量
        long pendingComments = commentService.count(
            new LambdaQueryWrapper<TravelNoteComment>().eq(TravelNoteComment::getReviewStatus, 0)
        );
        
        stats.put("pendingNotes", pendingNotes);
        stats.put("pendingCompanions", pendingCompanions);
        stats.put("pendingComments", pendingComments);
        stats.put("totalPending", pendingNotes + pendingCompanions + pendingComments);
        
        return Result.success(stats);
    }

    // ==================== 敏感词管理 ====================

    /**
     * 获取敏感词列表
     */
    @GetMapping("/sensitive-words")
    public Result<?> getSensitiveWords() {
        List<SensitiveWord> words = contentReviewService.getAllSensitiveWords();
        return Result.success(words);
    }

    /**
     * 添加敏感词
     */
    @PostMapping("/sensitive-word")
    public Result<?> addSensitiveWord(@RequestBody SensitiveWord word) {
        boolean success = contentReviewService.addSensitiveWord(word);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.error("敏感词已存在");
        }
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/sensitive-word/{id}")
    public Result<?> deleteSensitiveWord(@PathVariable Long id) {
        boolean success = contentReviewService.deleteSensitiveWord(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
