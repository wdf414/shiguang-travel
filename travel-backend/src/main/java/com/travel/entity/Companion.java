package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("companion")
public class Companion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long lineId;

    private String title;

    private String content;

    private LocalDate departureDate;

    private Integer memberCount;

    private Integer currentCount;

    private Integer status; // 0-招募中 1-已满员 2-已结束
    
    private Integer reviewStatus; // 0-待审核 1-已通过 2-已拒绝
    private LocalDateTime reviewTime; // 审核时间
    private Long reviewerId; // 审核人ID
    private String rejectReason; // 驳回原因

    private String school; // 限定学校（可选）

    private String groupQrcode; // 群聊二维码图片链接

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 非数据库字段
    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private String creatorSchool;

    @TableField(exist = false)
    private String creatorMajor;

    @TableField(exist = false)
    private String creatorGrade;

    @TableField(exist = false)
    private Integer isLeader; // 是否是领队发起

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getGroupQrcode() {
        return groupQrcode;
    }

    public void setGroupQrcode(String groupQrcode) {
        this.groupQrcode = groupQrcode;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorSchool() {
        return creatorSchool;
    }

    public void setCreatorSchool(String creatorSchool) {
        this.creatorSchool = creatorSchool;
    }

    public String getCreatorMajor() {
        return creatorMajor;
    }

    public void setCreatorMajor(String creatorMajor) {
        this.creatorMajor = creatorMajor;
    }

    public String getCreatorGrade() {
        return creatorGrade;
    }

    public void setCreatorGrade(String creatorGrade) {
        this.creatorGrade = creatorGrade;
    }

    public Integer getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(Integer isLeader) {
        this.isLeader = isLeader;
    }
}