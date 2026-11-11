package com.travel.service.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.common.PageResult;
import com.travel.dto.LineQueryDTO;
import com.travel.entity.TourLine;
import com.travel.exception.BusinessException;
import com.travel.mapper.TourLineMapper;

@Service
public class AdminLineService {
    @Autowired
    private TourLineMapper tourLineMapper;

    public PageResult<TourLine> getLineList(LineQueryDTO dto) {
        Page<TourLine> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<TourLine> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(TourLine::getTitle, dto.getKeyword());
        }
        if (StringUtils.hasText(dto.getDestination())) {
            wrapper.eq(TourLine::getDestination, dto.getDestination());
        }
        if (dto.getMinPrice() != null) {
            wrapper.ge(TourLine::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            wrapper.le(TourLine::getPrice, dto.getMaxPrice());
        }
        
        wrapper.orderByDesc(TourLine::getCreateTime);
        Page<TourLine> result = tourLineMapper.selectPage(page, wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), dto.getPage(), dto.getSize());
    }

    public TourLine getLineDetail(Long id) {
        TourLine line = tourLineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException("线路不存在");
        }
        return line;
    }

    public void createLine(TourLine line) {
        if (line.getStatus() == null) {
            line.setStatus(1);
        }
        if (line.getStock() == null) {
            line.setStock(100);
        }
        if (line.getSales() == null) {
            line.setSales(0);
        }
        if (line.getRating() == null) {
            line.setRating(java.math.BigDecimal.valueOf(5.0));
        }
        if (line.getReviewCount() == null) {
            line.setReviewCount(0);
        }
        tourLineMapper.insert(line);
    }

    public void updateLine(TourLine line) {
        TourLine existing = tourLineMapper.selectById(line.getId());
        if (existing == null) {
            throw new BusinessException("线路不存在");
        }
        tourLineMapper.updateById(line);
    }

    public void updateStatus(Long id, Integer status) {
        TourLine line = tourLineMapper.selectById(id);
        if (line == null) {
            throw new BusinessException("线路不存在");
        }
        line.setStatus(status);
        tourLineMapper.updateById(line);
    }

    public void deleteLine(Long id) {
        tourLineMapper.deleteById(id);
    }
}
