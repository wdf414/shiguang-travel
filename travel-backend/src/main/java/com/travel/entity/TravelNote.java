package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("travel_note")
public class TravelNote {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String coverImage;

    private String destination;

    private Long userId;

    private Integer days;

    private Double cost;

    private LocalDateTime travelDate;

    private Integer views;

    private Integer likes;

    private Integer favorites;

    private Integer comments;

    private Integer status; // 0-暂存 1-已发布
    
    private Integer reviewStatus; // 0-待审核 1-已通过 2-已拒绝
    private LocalDateTime reviewTime; // 审核时间
    private Long reviewerId; // 审核人ID
    private String rejectReason; // 驳回原因

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 非数据库字段，展示作者信息
    @TableField(exist = false)
    private User author;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public LocalDateTime getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDateTime travelDate) { this.travelDate = travelDate; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public Integer getFavorites() { return favorites; }
    public void setFavorites(Integer favorites) { this.favorites = favorites; }

    public Integer getComments() { return comments; }
    public void setComments(Integer comments) { this.comments = comments; }

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

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}
