package com.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.Result;
import com.travel.entity.TravelNote;
import com.travel.security.LoginUser;
import com.travel.service.TravelNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travel-notes")
public class TravelNoteController {

    @Autowired
    private TravelNoteService travelNoteService;

    @GetMapping
    public Result<Page<TravelNote>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String destination,
                                         @RequestParam(required = false) Integer status) {
        Page<TravelNote> pageParam = new Page<>(page, size);
        Page<TravelNote> result = travelNoteService.getNoteList(pageParam, destination, status);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<TravelNote> detail(@PathVariable Long id) {
        TravelNote note = travelNoteService.getNoteDetail(id);
        return Result.success(note);
    }

    @PostMapping
    public Result<Void> publish(@AuthenticationPrincipal LoginUser loginUser,
                                @RequestBody TravelNote note) {
        if (loginUser == null) {
            return Result.error(401, "请登录后再发布游记");
        }
        note.setUserId(loginUser.getUserId());
        travelNoteService.publishNote(note);
        return Result.success();
    }
}
