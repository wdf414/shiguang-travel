package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TravelNote;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TravelNoteMapper extends BaseMapper<TravelNote> {
}
