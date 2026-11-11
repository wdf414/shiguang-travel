package com.travel.controller;

import com.travel.common.Result;
import com.travel.entity.TravelNoteComment;
import com.travel.security.LoginUser;
import com.travel.service.TravelNoteCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/travel-note-comments")
public class TravelNoteCommentController {

    @Autowired
    private TravelNoteCommentService travelNoteCommentService;

    @GetMapping
    public Result<List<TravelNoteComment>> list(@RequestParam Long noteId) {
        List<TravelNoteComment> comments = travelNoteCommentService.getCommentsByNoteId(noteId);
        return Result.success(comments);
    }

    @PostMapping
    public Result<Void> add(@AuthenticationPrincipal LoginUser loginUser,
                            @RequestBody TravelNoteComment comment) {
        if (!StringUtils.hasText(comment.getContent())) {
            return Result.error("内容不能为空");
        }
        comment.setUserId(loginUser.getUserId());
        travelNoteCommentService.addComment(comment);
        return Result.success();
    }
}
