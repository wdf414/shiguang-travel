package com.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.travel.common.PageResult;
import com.travel.entity.Guide;
import com.travel.entity.Hotel;
import com.travel.entity.Review;
import com.travel.entity.TourLine;
import com.travel.mapper.GuideMapper;
import com.travel.mapper.HotelMapper;
import com.travel.mapper.ReviewMapper;
import com.travel.mapper.TourLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReviewService {
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private TourLineMapper tourLineMapper;
    @Autowired
    private HotelMapper hotelMapper;
    @Autowired
    private GuideMapper guideMapper;

    public PageResult<Review> getReviewList(Long lineId, Long hotelId, Long guideId, Boolean onlyStudent, int page, int size) {
        Page<Review> pageParam = new Page<>(page, size);
        IPage<Review> result = reviewMapper.selectReviews(pageParam, lineId, hotelId, guideId, onlyStudent);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Transactional
    public void submitReview(Long userId, Long lineId, Long hotelId, Long guideId, Long orderId, Integer rating, String content, String images, String tags) {
        Review review = new Review();
        review.setUserId(userId);
        review.setLineId(lineId);
        review.setHotelId(hotelId);
        review.setGuideId(guideId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setTags(tags);
        reviewMapper.insert(review);
        
        // Update ratings
        if (lineId != null) {
            updateLineRating(lineId);
        }
        if (hotelId != null) {
            updateHotelRating(hotelId);
        }
        if (guideId != null) {
            updateGuideRating(guideId);
        }
    }

    private void updateLineRating(Long lineId) {
        Double avgRating = reviewMapper.calculateAvgRating(lineId);
        Integer reviewCount = reviewMapper.countByLineId(lineId);
        
        TourLine line = tourLineMapper.selectById(lineId);
        if (line == null) return;
        
        line.setReviewCount(reviewCount != null ? reviewCount : 0);
        line.setRating(avgRating != null ? new BigDecimal(avgRating.toString()).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        tourLineMapper.updateById(line);
    }

    private void updateHotelRating(Long hotelId) {
        Double avgRating = reviewMapper.calculateHotelAvgRating(hotelId);
        Integer reviewCount = reviewMapper.countByHotelId(hotelId);
        
        Hotel hotel = hotelMapper.selectById(hotelId);
        if (hotel == null) return;
        
        hotel.setReviewCount(reviewCount != null ? reviewCount : 0);
        hotel.setRating(avgRating != null ? new BigDecimal(avgRating.toString()).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        hotelMapper.updateById(hotel);
    }

    private void updateGuideRating(Long guideId) {
        Double avgRating = reviewMapper.calculateGuideAvgRating(guideId);
        Integer reviewCount = reviewMapper.countByGuideId(guideId);
        
        Guide guide = guideMapper.selectById(guideId);
        if (guide == null) return;
        
        guide.setReviewCount(reviewCount != null ? reviewCount : 0);
        guide.setRating(avgRating != null ? new BigDecimal(avgRating.toString()).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        guideMapper.updateById(guide);
    }
}
