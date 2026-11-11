package com.travel.controller.admin;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Complaint;
import com.travel.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaint")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/list")
    public Result<PageResult<Complaint>> getComplaintList(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) Integer type,
                                                          @RequestParam(required = false) Integer status) {
        return Result.success(complaintService.getComplaintPage(page, size, type, status));
    }

    @PostMapping("/{id}/reply")
    public Result<Void> replyComplaint(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        if (reply == null || reply.trim().isEmpty()) {
            return Result.error("回复内容不能为空");
        }
        complaintService.reply(id, reply);
        return Result.success();
    }
}
