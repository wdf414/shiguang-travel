package com.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.dto.LineQueryDTO;
import com.travel.entity.Favorite;
import com.travel.entity.TourLine;
import com.travel.exception.BusinessException;
import com.travel.mapper.FavoriteMapper;
import com.travel.mapper.TourLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TourLineService {
    @Autowired
    private TourLineMapper tourLineMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String HOT_LINES_KEY = "travel:hot_lines";

    public PageResult<TourLine> getLineList(LineQueryDTO dto) {
        Page<TourLine> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<TourLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TourLine::getStatus, 1);
        
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.and(w -> w.like(TourLine::getTitle, dto.getKeyword())
                    .or().like(TourLine::getDestination, dto.getKeyword()));
        }
        if (StringUtils.hasText(dto.getDestination())) {
            wrapper.eq(TourLine::getDestination, dto.getDestination());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            wrapper.eq(TourLine::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getDestinations())) {
            List<String> destList = Arrays.asList(dto.getDestinations().split(","));
            wrapper.in(TourLine::getDestination, destList);
        }
        if (StringUtils.hasText(dto.getDepartureCity())) {
            wrapper.eq(TourLine::getDepartureCity, dto.getDepartureCity());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(TourLine::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(TourLine::getPrice, dto.getMaxPrice());
        }
        if (dto.getMinDays() != null) {
            wrapper.ge(TourLine::getDays, dto.getMinDays());
        }
        if (dto.getMaxDays() != null) {
            wrapper.le(TourLine::getDays, dto.getMaxDays());
        }
        if (StringUtils.hasText(dto.getTags())) {
            String[] tagArray = dto.getTags().split(",");
            wrapper.and(w -> {
                for (int i = 0; i < tagArray.length; i++) {
                    String tag = tagArray[i];
                    if (i == 0) {
                        w.like(TourLine::getTags, tag);
                    } else {
                        w.or().like(TourLine::getTags, tag);
                    }
                }
            });
        }
        
        // 排序
        if ("price_asc".equals(dto.getSortBy())) {
            wrapper.orderByAsc(TourLine::getPrice);
        } else if ("price_desc".equals(dto.getSortBy())) {
            wrapper.orderByDesc(TourLine::getPrice);
        } else if ("rating".equals(dto.getSortBy())) {
            wrapper.orderByDesc(TourLine::getRating);
        } else if ("sales".equals(dto.getSortBy())) {
            wrapper.orderByDesc(TourLine::getSales);
        } else {
            wrapper.orderByDesc(TourLine::getCreateTime);
        }
        
        Page<TourLine> result = tourLineMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), dto.getPage(), dto.getSize());
    }

    public TourLine getLineDetail(Long id) {
        TourLine line = tourLineMapper.selectById(id);
        if (line == null || line.getStatus() != 1) {
            throw new BusinessException("线路不存在");
        }
        return line;
    }

    @SuppressWarnings("unchecked")
    public List<TourLine> getHotLines() {
        // 先从缓存获取
        List<TourLine> hotLines = (List<TourLine>) redisTemplate.opsForValue().get(HOT_LINES_KEY);
        if (hotLines != null) {
            return hotLines;
        }
        
        // 查询数据库
        LambdaQueryWrapper<TourLine> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TourLine::getStatus, 1)
                .orderByDesc(TourLine::getSales)
                .last("LIMIT 10");
        hotLines = tourLineMapper.selectList(wrapper);
        
        // 缓存10分钟
        redisTemplate.opsForValue().set(HOT_LINES_KEY, hotLines, 10, TimeUnit.MINUTES);
        return hotLines;
    }

    public List<String> getDestinations() {
        return tourLineMapper.selectDestinations();
    }

    public void favorite(Long userId, Long lineId) {
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getLineId, lineId));
        if (count > 0) {
            throw new BusinessException("已收藏该线路");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setLineId(lineId);
        favoriteMapper.insert(favorite);
    }

    public void unfavorite(Long userId, Long lineId) {
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getLineId, lineId));
    }

    public PageResult<TourLine> getFavorites(Long userId, int page, int size) {
        Page<Favorite> favPage = new Page<>(page, size);
        Page<Favorite> favResult = favoriteMapper.selectPage(favPage, 
                new LambdaQueryWrapper<Favorite>()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime));
        
        List<Long> lineIds = favResult.getRecords().stream()
                .map(Favorite::getLineId).toList();
        
        if (lineIds.isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }
        
        List<TourLine> lines = tourLineMapper.selectBatchIds(lineIds);
        return PageResult.of(lines, favResult.getTotal(), page, size);
    }

    public boolean isFavorite(Long userId, Long lineId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getLineId, lineId)) > 0;
    }
}
