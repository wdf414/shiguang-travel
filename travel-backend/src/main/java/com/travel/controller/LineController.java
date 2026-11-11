package com.travel.controller;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.dto.LineQueryDTO;
import com.travel.entity.TourLine;
import com.travel.security.LoginUser;
import com.travel.service.TourLineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/line")
public class LineController {
    @Autowired
    private TourLineService tourLineService;

    @GetMapping("/list")
    public Result<PageResult<TourLine>> getLineList(LineQueryDTO dto) {
        return Result.success(tourLineService.getLineList(dto));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> getLineDetail(@PathVariable Long id,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        TourLine line = tourLineService.getLineDetail(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", line.getId());
        result.put("title", line.getTitle());
        result.put("coverImage", line.getCoverImage());
        result.put("images", line.getImages());
        result.put("destination", line.getDestination());
        result.put("departureCity", line.getDepartureCity());
        result.put("price", line.getPrice());
        result.put("days", line.getDays());
        result.put("description", line.getDescription());
        result.put("schedule", line.getSchedule());
        result.put("includes", line.getIncludes());
        result.put("excludes", line.getExcludes());
        result.put("stock", line.getStock());
        result.put("sales", line.getSales());
        result.put("rating", line.getRating());
        result.put("reviewCount", line.getReviewCount());
        
        if (loginUser != null) {
            result.put("isFavorite", tourLineService.isFavorite(loginUser.getUserId(), id));
        } else {
            result.put("isFavorite", false);
        }
        return Result.success(result);
    }

    @GetMapping("/hot")
    public Result<List<TourLine>> getHotLines() {
        return Result.success(tourLineService.getHotLines());
    }

    @GetMapping("/destinations")
    public Result<List<String>> getDestinations() {
        return Result.success(tourLineService.getDestinations());
    }

    @PostMapping("/{id}/favorite")
    public Result<Void> favorite(@PathVariable Long id,
                                 @AuthenticationPrincipal LoginUser loginUser) {
        tourLineService.favorite(loginUser.getUserId(), id);
        return Result.success();
    }

    @DeleteMapping("/{id}/favorite")
    public Result<Void> unfavorite(@PathVariable Long id,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        tourLineService.unfavorite(loginUser.getUserId(), id);
        return Result.success();
    }

    @GetMapping("/favorites")
    public Result<PageResult<TourLine>> getFavorites(@AuthenticationPrincipal LoginUser loginUser,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return Result.success(tourLineService.getFavorites(loginUser.getUserId(), page, size));
    }
}
