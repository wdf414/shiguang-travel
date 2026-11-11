package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.common.PageResult;
import com.travel.entity.Complaint;

public interface ComplaintService extends IService<Complaint> {
    PageResult<Complaint> getComplaintPage(Integer page, Integer size, Integer type, Integer status);
    PageResult<Complaint> getUserComplaintPage(Long userId, Integer page, Integer size);
    void reply(Long id, String reply);
}
