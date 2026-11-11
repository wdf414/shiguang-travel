package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.common.PageResult;
import com.travel.entity.Complaint;
import com.travel.entity.User;
import com.travel.mapper.ComplaintMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<Complaint> getComplaintPage(Integer page, Integer size, Integer type, Integer status) {
        Page<Complaint> p = new Page<>(page, size);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, Complaint::getType, type)
               .eq(status != null, Complaint::getStatus, status)
               .orderByDesc(Complaint::getCreateTime);
        
        Page<Complaint> complaintPage = page(p, wrapper);
        
        // 填充用户信息
        List<Complaint> records = complaintPage.getRecords();
        if (!records.isEmpty()) {
            List<Long> userIds = records.stream().map(Complaint::getUserId).collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, String> userMap = users.stream().collect(Collectors.toMap(User::getId, User::getNickname));
            
            for (Complaint complaint : records) {
                complaint.setNickname(userMap.get(complaint.getUserId()));
            }
        }
        
        return PageResult.of(records, complaintPage.getTotal(), page, size);
    }

    @Override
    public PageResult<Complaint> getUserComplaintPage(Long userId, Integer page, Integer size) {
        Page<Complaint> p = new Page<>(page, size);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Complaint::getUserId, userId)
               .orderByDesc(Complaint::getCreateTime);
        
        Page<Complaint> complaintPage = page(p, wrapper);
        return PageResult.of(complaintPage.getRecords(), complaintPage.getTotal(), page, size);
    }

    @Override
    public void reply(Long id, String reply) {
        Complaint complaint = getById(id);
        if (complaint != null) {
            complaint.setReply(reply);
            complaint.setStatus(1); // 已回复
            complaint.setReplyTime(LocalDateTime.now());
            updateById(complaint);
        }
    }
}
