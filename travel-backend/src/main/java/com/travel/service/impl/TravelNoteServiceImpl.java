package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.TravelNote;
import com.travel.entity.User;
import com.travel.mapper.TravelNoteMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.TravelNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class TravelNoteServiceImpl extends ServiceImpl<TravelNoteMapper, TravelNote> implements TravelNoteService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<TravelNote> getNoteList(Page<TravelNote> page, String destination, Integer status) {
        LambdaQueryWrapper<TravelNote> wrapper = new LambdaQueryWrapper<>();
        // 如果指定了状态，按状态查询；否则默认查询已通过审核的内容
        wrapper.eq(status != null, TravelNote::getStatus, status);
        wrapper.eq(status == null, TravelNote::getStatus, 1); // 默认只查询已通过审核的
        wrapper.eq(StringUtils.hasText(destination), TravelNote::getDestination, destination);
        wrapper.orderByDesc(TravelNote::getCreateTime);
        
        Page<TravelNote> result = this.page(page, wrapper);
        
        // 填充作者信息
        result.getRecords().forEach(note -> {
            User author = userMapper.selectById(note.getUserId());
            if (author != null) {
                author.setPassword(null); // 隐藏敏感信息
                note.setAuthor(author);
            }
        });
        
        return result;
    }

    @Override
    public TravelNote getNoteDetail(Long id) {
        TravelNote note = this.getById(id);
        if (note != null) {
            // 更新浏览量（简单实现，实际高并发应使用Redis）
            note.setViews(note.getViews() + 1);
            this.updateById(note);
            
            // 填充作者信息
            User author = userMapper.selectById(note.getUserId());
            if (author != null) {
                author.setPassword(null);
                note.setAuthor(author);
            }
        }
        return note;
    }

    @Override
    public void publishNote(TravelNote note) {
        note.setViews(0);
        note.setLikes(0);
        note.setFavorites(0);
        note.setComments(0);
        note.setStatus(0); // 默认为待审核状态
        this.save(note);
    }
}
