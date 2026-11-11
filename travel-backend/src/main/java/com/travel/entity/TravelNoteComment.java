package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("travel_note_comment")
public class TravelNoteComment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long userId;

    private String content;

    private Long replyToUid;

    private Integer likes;

    private Integer status; // 0-屏蔽 1-正常
    
    private Integer reviewStatus; // 0-待审核 1-已通过 2-已拒绝
    private LocalDateTime reviewTime; // 审核时间
    private Long reviewerId; // 审核人ID
    private String rejectReason; // 驳回原因

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private User replyToUser;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getReplyToUid() { return replyToUid; }
    public void setReplyToUid(Long replyToUid) { this.replyToUid = replyToUid; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(Integer reviewStatus) { this.reviewStatus = reviewStatus; }

    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public User getReplyToUser() { return replyToUser; }
    public void setReplyToUser(User replyToUser) { this.replyToUser = replyToUser; }
}
