package com.travel.controller;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Review;
import com.travel.security.LoginUser;
import com.travel.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @GetMapping("/list")
    public Result<PageResult<Review>> getReviewList(
            @RequestParam(required = false) Long lineId,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Long guideId,
            @RequestParam(required = false) Boolean onlyStudent,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getReviewList(lineId, hotelId, guideId, onlyStudent, page, size));
    }
    
    // 兼容旧接口
    @GetMapping("/line/{lineId}")
    public Result<PageResult<Review>> getLineReviewList(@PathVariable Long lineId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.getReviewList(lineId, null, null, null, page, size));
    }

    @PostMapping("/submit")
    public Result<Void> submitReview(@AuthenticationPrincipal LoginUser loginUser,
                                     @RequestBody Map<String, Object> body) {
        Long lineId = body.get("lineId") != null ? Long.valueOf(body.get("lineId").toString()) : null;
        Long hotelId = body.get("hotelId") != null ? Long.valueOf(body.get("hotelId").toString()) : null;
        Long guideId = body.get("guideId") != null ? Long.valueOf(body.get("guideId").toString()) : null;
        Long orderId = body.get("orderId") != null ? Long.valueOf(body.get("orderId").toString()) : null;
        
        reviewService.submitReview(
                loginUser.getUserId(),
                lineId,
                hotelId,
                guideId,
                orderId,
                Integer.valueOf(body.get("rating").toString()),
                (String) body.get("content"),
                (String) body.get("images"),
                (String) body.get("tags")
        );
        return Result.success();
    }
}
