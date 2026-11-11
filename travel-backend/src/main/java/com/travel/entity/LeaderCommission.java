package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("leader_commission")
public class LeaderCommission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long leaderId;

    private Long companionId;

    private BigDecimal orderAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private Integer status; // 0-待结算 1-已结算

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // 非数据库字段
    @TableField(exist = false)
    private String companionTitle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public Long getCompanionId() {
        return companionId;
    }

    public void setCompanionId(Long companionId) {
        this.companionId = companionId;
    }

    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
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

    public String getCompanionTitle() {
        return companionTitle;
    }

    public void setCompanionTitle(String companionTitle) {
        this.companionTitle = companionTitle;
    }
}
