package com.travel.controller;

import com.travel.common.PageResult;
import com.travel.common.Result;
import com.travel.entity.Complaint;
import com.travel.security.LoginUser;
import com.travel.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @PostMapping("/add")
    public Result<Void> addComplaint(@AuthenticationPrincipal LoginUser loginUser, @RequestBody Complaint complaint) {
        complaint.setUserId(loginUser.getUserId());
        complaint.setStatus(0); // 待处理
        complaintService.save(complaint);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<PageResult<Complaint>> getMyComplaints(@AuthenticationPrincipal LoginUser loginUser,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(complaintService.getUserComplaintPage(loginUser.getUserId(), page, size));
    }
}
