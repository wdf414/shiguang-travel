package com.travel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.entity.ReviewRecord;
import com.travel.entity.SensitiveWord;

import java.util.List;

public interface ContentReviewService extends IService<ReviewRecord> {
    
    /**
     * 检测文本中的敏感词
     */
    List<String> checkSensitiveWords(String text);
    
    /**
     * 自动审核内容
     * @return true-通过 false-不通过
     */
    boolean autoReview(String content);
    
    /**
     * 人工审核内容
     */
    void manualReview(String contentType, Long contentId, Integer newStatus, 
                      String rejectReason, Long reviewerId, String reviewerName);
    
    /**
     * 获取审核记录列表
     */
    List<ReviewRecord> getReviewRecords(String contentType, Long contentId);
    
    /**
     * 添加敏感词
     */
    boolean addSensitiveWord(SensitiveWord word);
    
    /**
     * 删除敏感词
     */
    boolean deleteSensitiveWord(Long id);
    
    /**
     * 获取所有敏感词
     */
    List<SensitiveWord> getAllSensitiveWords();
}
