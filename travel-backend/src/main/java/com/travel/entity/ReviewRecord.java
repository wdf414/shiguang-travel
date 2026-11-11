package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@TableName("review_record")
public class ReviewRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String contentType;
    private Long contentId;
    private String contentTitle;
    private Long contentAuthorId;
    
    private Integer oldStatus;
    private Integer newStatus;
    
    private Integer reviewType;
    private Long reviewerId;
    private String reviewerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
    
    private String rejectReason;
    
    private String autoReviewResult;
    private String sensitiveWords;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }

    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }

    public Long getContentAuthorId() { return contentAuthorId; }
    public void setContentAuthorId(Long contentAuthorId) { this.contentAuthorId = contentAuthorId; }

    public Integer getOldStatus() { return oldStatus; }
    public void setOldStatus(Integer oldStatus) { this.oldStatus = oldStatus; }

    public Integer getNewStatus() { return newStatus; }
    public void setNewStatus(Integer newStatus) { this.newStatus = newStatus; }

    public Integer getReviewType() { return reviewType; }
    public void setReviewType(Integer reviewType) { this.reviewType = reviewType; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getAutoReviewResult() { return autoReviewResult; }
    public void setAutoReviewResult(String autoReviewResult) { this.autoReviewResult = autoReviewResult; }

    public String getSensitiveWords() { return sensitiveWords; }
    public void setSensitiveWords(String sensitiveWords) { this.sensitiveWords = sensitiveWords; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
