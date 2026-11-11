package com.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.entity.*;
import com.travel.mapper.ReviewRecordMapper;
import com.travel.mapper.SensitiveWordMapper;
import com.travel.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ContentReviewServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord> 
        implements ContentReviewService {

    private static final Logger log = LoggerFactory.getLogger(ContentReviewServiceImpl.class);

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;
    
    @Autowired
    private TravelNoteService travelNoteService;
    
    @Autowired
    private CompanionService companionService;
    
    @Autowired
    private TravelNoteCommentService commentService;

    @Override
    public List<String> checkSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取所有启用的敏感词
        List<SensitiveWord> words = sensitiveWordMapper.selectList(
            new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getIsActive, 1)
        );
        
        List<String> foundWords = new ArrayList<>();
        String lowerText = text.toLowerCase();
        
        for (SensitiveWord word : words) {
            String sensitiveWord = word.getWord().toLowerCase();
            
            if (word.getMatchMode() == 1) {
                // 精确匹配
                if (lowerText.contains(sensitiveWord)) {
                    foundWords.add(word.getWord());
                }
            } else {
                // 模糊匹配（使用正则）
                Pattern pattern = Pattern.compile(sensitiveWord);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    foundWords.add(word.getWord());
                }
            }
        }
        
        return foundWords;
    }

    @Override
    public boolean autoReview(String content) {
        List<String> sensitiveWords = checkSensitiveWords(content);
        
        // 如果包含违禁词（type=1），直接不通过
        List<SensitiveWord> allWords = sensitiveWordMapper.selectList(
            new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getIsActive, 1)
        );
        
        for (String word : sensitiveWords) {
            Optional<SensitiveWord> sw = allWords.stream()
                .filter(w -> w.getWord().equals(word))
                .findFirst();
            
            if (sw.isPresent() && sw.get().getWordType() == 1) {
                // 违禁词，不通过
                return false;
            }
        }
        
        // 只包含敏感词或广告词，可以通过但记录
        return true;
    }

    @Override
    @Transactional
    public void manualReview(String contentType, Long contentId, Integer newStatus,
                            String rejectReason, Long reviewerId, String reviewerName) {
        // 获取原内容
        Integer oldStatus = null;
        String contentTitle = null;
        Long authorId = null;
        
        switch (contentType) {
            case "travel_note":
                TravelNote note = travelNoteService.getById(contentId);
                if (note != null) {
                    oldStatus = note.getStatus();
                    contentTitle = note.getTitle();
                    authorId = note.getUserId();
                    
                    // 更新状态
                    note.setStatus(newStatus);
                    note.setReviewerId(reviewerId);
                    note.setReviewTime(LocalDateTime.now());
                    note.setRejectReason(rejectReason);
                    travelNoteService.updateById(note);
                }
                break;
            case "companion":
                Companion companion = companionService.getById(contentId);
                if (companion != null) {
                    oldStatus = companion.getStatus();
                    contentTitle = companion.getTitle();
                    authorId = companion.getUserId();
                    
                    companion.setStatus(newStatus);
                    companion.setReviewerId(reviewerId);
                    companion.setReviewTime(LocalDateTime.now());
                    companion.setRejectReason(rejectReason);
                    companionService.updateById(companion);
                }
                break;
            case "comment":
                TravelNoteComment comment = commentService.getById(contentId);
                if (comment != null) {
                    oldStatus = comment.getStatus();
                    contentTitle = comment.getContent();
                    authorId = comment.getUserId();
                    
                    comment.setStatus(newStatus);
                    comment.setReviewerId(reviewerId);
                    comment.setReviewTime(LocalDateTime.now());
                    comment.setRejectReason(rejectReason);
                    commentService.updateById(comment);
                }
                break;
        }
        
        // 创建审核记录
        ReviewRecord record = new ReviewRecord();
        record.setContentType(contentType);
        record.setContentId(contentId);
        record.setContentTitle(contentTitle);
        record.setContentAuthorId(authorId);
        record.setOldStatus(oldStatus);
        record.setNewStatus(newStatus);
        record.setReviewType(1); // 人工审核
        record.setReviewerId(reviewerId);
        record.setReviewerName(reviewerName);
        record.setReviewTime(LocalDateTime.now());
        record.setRejectReason(rejectReason);
        
        this.save(record);
        
        log.info("内容审核完成：type={}, id={}, oldStatus={}, newStatus={}, reviewer={}", 
                contentType, contentId, oldStatus, newStatus, reviewerName);
    }

    @Override
    public List<ReviewRecord> getReviewRecords(String contentType, Long contentId) {
        return this.list(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getContentType, contentType)
                .eq(ReviewRecord::getContentId, contentId)
                .orderByDesc(ReviewRecord::getCreateTime));
    }

    @Override
    public boolean addSensitiveWord(SensitiveWord word) {
        // 检查是否已存在
        Long count = sensitiveWordMapper.selectCount(
            new LambdaQueryWrapper<SensitiveWord>()
                .eq(SensitiveWord::getWord, word.getWord())
        );
        if (count > 0) {
            return false;
        }
        return sensitiveWordMapper.insert(word) > 0;
    }

    @Override
    public boolean deleteSensitiveWord(Long id) {
        return sensitiveWordMapper.deleteById(id) > 0;
    }

    @Override
    public List<SensitiveWord> getAllSensitiveWords() {
        return sensitiveWordMapper.selectList(null);
    }
}
