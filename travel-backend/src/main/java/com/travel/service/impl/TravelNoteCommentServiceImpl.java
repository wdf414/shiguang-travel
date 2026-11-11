package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.TravelNote;
import com.travel.entity.TravelNoteComment;
import com.travel.entity.User;
import com.travel.mapper.TravelNoteCommentMapper;
import com.travel.mapper.TravelNoteMapper;
import com.travel.mapper.UserMapper;
import com.travel.service.TravelNoteCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TravelNoteCommentServiceImpl extends ServiceImpl<TravelNoteCommentMapper, TravelNoteComment> implements TravelNoteCommentService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TravelNoteMapper travelNoteMapper;

    @Override
    public List<TravelNoteComment> getCommentsByNoteId(Long noteId) {
        LambdaQueryWrapper<TravelNoteComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TravelNoteComment::getNoteId, noteId);
        wrapper.eq(TravelNoteComment::getStatus, 1);
        wrapper.orderByDesc(TravelNoteComment::getCreateTime);
        
        List<TravelNoteComment> comments = this.list(wrapper);
        
        // 填充用户信息
        comments.forEach(comment -> {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                user.setPassword(null);
                comment.setUser(user);
            }
            // 如果是回复，填充被回复人信息
            if (comment.getReplyToUid() != null) {
                User replyUser = userMapper.selectById(comment.getReplyToUid());
                if (replyUser != null) {
                    replyUser.setPassword(null);
                    comment.setReplyToUser(replyUser);
                }
            }
        });
        return comments;
    }

    @Override
    @Transactional
    public void addComment(TravelNoteComment comment) {
        comment.setLikes(0);
        comment.setStatus(1);
        this.save(comment);
        
        // 更新游记评论数
        TravelNote note = travelNoteMapper.selectById(comment.getNoteId());
        if (note != null) {
            note.setComments(note.getComments() + 1);
            travelNoteMapper.updateById(note);
        }
    }
}
