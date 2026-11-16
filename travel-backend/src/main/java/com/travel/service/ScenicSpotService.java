package com.travel.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.entity.ScenicSpot;
import com.travel.mapper.ScenicSpotMapper;

@Service
public class ScenicSpotService {
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    public PageResult<Map<String, Object>> search(String city, String keyword, int page, int size) {
        Page<ScenicSpot> p = new Page<>(page, size);
        LambdaQueryWrapper<ScenicSpot> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(city)) {
            wrapper.eq(ScenicSpot::getSearchCity, city);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ScenicSpot::getName, keyword)
                    .or().like(ScenicSpot::getAddress, keyword)
                    .or().like(ScenicSpot::getCity, keyword));
        }

        wrapper.orderByAsc(ScenicSpot::getCity).orderByAsc(ScenicSpot::getName);

        Page<ScenicSpot> result = scenicSpotMapper.selectPage(p, wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(this::toView).toList();
        return PageResult.of(records, result.getTotal(), page, size);
    }

    private Map<String, Object> toView(ScenicSpot spot) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", spot.getId());
        map.put("name", spot.getName());
        map.put("province", spot.getProvince());
        map.put("city", spot.getCity());
        map.put("district", spot.getDistrict());
        map.put("address", spot.getAddress());
        map.put("latitude", spot.getLatitude());
        map.put("longitude", spot.getLongitude());
        map.put("tag", spot.getTag());
        map.put("type", spot.getType());
        map.put("searchCity", spot.getSearchCity());
        String cover = null;
        if (StringUtils.hasText(spot.getName())) {
            String encoded = URLEncoder.encode(spot.getName(), StandardCharsets.UTF_8);
            cover = "/api/scenic-spot/photo?name=" + encoded;
        }
        if (!StringUtils.hasText(cover)) {
            long seed = spot.getId() != null ? spot.getId() : (spot.getName() != null ? spot.getName().hashCode() : 1);
            cover = "/api/photos/pick?seed=scenic-" + Math.abs(seed);
        }
        map.put("coverImage", cover);
        return map;
    }
}
