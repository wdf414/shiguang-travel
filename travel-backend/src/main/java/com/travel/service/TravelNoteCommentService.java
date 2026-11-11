package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.TravelNoteComment;
import java.util.List;

public interface TravelNoteCommentService extends IService<TravelNoteComment> {
    
    List<TravelNoteComment> getCommentsByNoteId(Long noteId);
    
    void addComment(TravelNoteComment comment);
}
