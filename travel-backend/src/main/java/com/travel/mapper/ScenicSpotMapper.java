package com.travel.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.entity.ScenicSpot;

public interface ScenicSpotMapper extends BaseMapper<ScenicSpot> {
    @Select("""
            SELECT photo_path
            FROM scenic_photos
            WHERE scenic_name = #{name}
            ORDER BY id ASC
            """)
    List<String> selectPhotoPathsByName(@Param("name") String name);
}
