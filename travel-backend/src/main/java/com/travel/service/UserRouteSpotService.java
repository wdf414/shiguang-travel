package com.travel.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.entity.ScenicSpot;
import com.travel.entity.UserRouteSpot;
import com.travel.exception.BusinessException;
import com.travel.mapper.ScenicSpotMapper;
import com.travel.mapper.UserRouteSpotMapper;

@Service
public class UserRouteSpotService {
    @Autowired
    private UserRouteSpotMapper userRouteSpotMapper;
    @Autowired
    private ScenicSpotMapper scenicSpotMapper;

    public void addSpot(Long userId, Long scenicSpotId) {
        ScenicSpot spot = scenicSpotMapper.selectById(scenicSpotId);
        if (spot == null) {
            throw new BusinessException("景点不存在");
        }
        Long count = userRouteSpotMapper.selectCount(new LambdaQueryWrapper<UserRouteSpot>()
                .eq(UserRouteSpot::getUserId, userId)
                .eq(UserRouteSpot::getScenicSpotId, scenicSpotId));
        if (count != null && count > 0) {
            throw new BusinessException("已加入路线");
        }

        UserRouteSpot routeSpot = new UserRouteSpot();
        routeSpot.setUserId(userId);
        routeSpot.setScenicSpotId(scenicSpotId);
        Integer maxSort = userRouteSpotMapper.selectList(
                new LambdaQueryWrapper<UserRouteSpot>()
                        .eq(UserRouteSpot::getUserId, userId)
                        .orderByDesc(UserRouteSpot::getSortOrder)
                        .last("LIMIT 1")
        ).stream().map(UserRouteSpot::getSortOrder).findFirst().orElse(0);
        routeSpot.setSortOrder(maxSort == null ? 1 : maxSort + 1);
        userRouteSpotMapper.insert(routeSpot);
    }

    public PageResult<Map<String, Object>> list(Long userId, int page, int size) {
        Page<UserRouteSpot> p = new Page<>(page, size);
        Page<UserRouteSpot> result = userRouteSpotMapper.selectPage(p,
                new LambdaQueryWrapper<UserRouteSpot>()
                        .eq(UserRouteSpot::getUserId, userId)
                        .orderByAsc(UserRouteSpot::getSortOrder)
                        .orderByDesc(UserRouteSpot::getCreateTime));

        if (result.getRecords().isEmpty()) {
            return PageResult.of(List.of(), 0, page, size);
        }

        List<Long> scenicIds = result.getRecords().stream()
                .map(UserRouteSpot::getScenicSpotId)
                .toList();
        List<ScenicSpot> spots = scenicSpotMapper.selectBatchIds(scenicIds);
        Map<Long, ScenicSpot> spotMap = new HashMap<>();
        for (ScenicSpot s : spots) {
            spotMap.put(s.getId(), s);
        }

        List<Map<String, Object>> records = result.getRecords().stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("sortOrder", r.getSortOrder());
            map.put("createTime", r.getCreateTime());
            ScenicSpot s = spotMap.get(r.getScenicSpotId());
            if (s != null) {
                map.put("scenicId", s.getId());
                map.put("name", s.getName());
                map.put("province", s.getProvince());
                map.put("city", s.getCity());
                map.put("district", s.getDistrict());
                map.put("address", s.getAddress());
                map.put("latitude", s.getLatitude());
                map.put("longitude", s.getLongitude());
                map.put("tag", s.getTag());
                map.put("type", s.getType());
                map.put("searchCity", s.getSearchCity());
                String cover = s.getCoverImage();
                if (cover == null || cover.isEmpty()) {
                    long seed = s.getId() != null ? s.getId() : s.getName().hashCode();
                    cover = "https://picsum.photos/seed/scenic-" + Math.abs(seed) + "/400/300";
                }
                map.put("coverImage", cover);
            }
            return map;
        }).toList();

        return PageResult.of(records, result.getTotal(), page, size);
    }

    public void remove(Long userId, Long id) {
        UserRouteSpot routeSpot = userRouteSpotMapper.selectById(id);
        if (routeSpot == null || !routeSpot.getUserId().equals(userId)) {
            throw new BusinessException("记录不存在");
        }
        userRouteSpotMapper.deleteById(id);
    }
}

