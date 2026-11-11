package com.travel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    @Select("<script>" +
            "SELECT r.*, u.nickname, u.avatar, u.is_student " +
            "FROM review r " +
            "LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE r.deleted = 0 " +
            "<if test='lineId != null'> AND r.line_id = #{lineId} </if>" +
            "<if test='hotelId != null'> AND r.hotel_id = #{hotelId} </if>" +
            "<if test='guideId != null'> AND r.guide_id = #{guideId} </if>" +
            "<if test='onlyStudent != null and onlyStudent == true'> AND u.is_student = 1 </if>" +
            "ORDER BY r.create_time DESC" +
            "</script>")
    IPage<Review> selectReviews(IPage<Review> page, 
                                @Param("lineId") Long lineId, 
                                @Param("hotelId") Long hotelId, 
                                @Param("guideId") Long guideId, 
                                @Param("onlyStudent") Boolean onlyStudent);
    
    @Select("SELECT AVG(rating) FROM review WHERE line_id = #{lineId} AND deleted = 0")
    Double calculateAvgRating(Long lineId);
    
    @Select("SELECT COUNT(*) FROM review WHERE line_id = #{lineId} AND deleted = 0")
    Integer countByLineId(Long lineId);

    @Select("SELECT AVG(rating) FROM review WHERE hotel_id = #{hotelId} AND deleted = 0")
    Double calculateHotelAvgRating(Long hotelId);
    
    @Select("SELECT COUNT(*) FROM review WHERE hotel_id = #{hotelId} AND deleted = 0")
    Integer countByHotelId(Long hotelId);

    @Select("SELECT AVG(rating) FROM review WHERE guide_id = #{guideId} AND deleted = 0")
    Double calculateGuideAvgRating(Long guideId);
    
    @Select("SELECT COUNT(*) FROM review WHERE guide_id = #{guideId} AND deleted = 0")
    Integer countByGuideId(Long guideId);
}
