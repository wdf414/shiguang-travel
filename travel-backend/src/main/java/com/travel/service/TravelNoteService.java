package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.TravelNote;

public interface TravelNoteService extends IService<TravelNote> {
    Page<TravelNote> getNoteList(Page<TravelNote> page, String destination, Integer status);

    TravelNote getNoteDetail(Long id);

    void publishNote(TravelNote note);
}

