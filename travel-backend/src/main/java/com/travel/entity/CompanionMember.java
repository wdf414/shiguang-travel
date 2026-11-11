package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

@TableName("companion_member")
public class CompanionMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companionId;

    private Long userId;

    private Integer status; // 0-待确认 1-已加入 2-已退出

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public void setCompanionId(Long companionId) {
        this.companionId = companionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}