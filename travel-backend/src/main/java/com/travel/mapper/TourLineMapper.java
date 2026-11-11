package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.TourLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TourLineMapper extends BaseMapper<TourLine> {
    @Select("SELECT DISTINCT destination FROM tour_line WHERE deleted = 0 AND status = 1")
    List<String> selectDestinations();
}
