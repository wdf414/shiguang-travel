package com.travel.vo;

import java.util.List;

public class ContentReviewVO {
    private String contentType;
    private Long contentId;
    private List<Long> contentIds;
    private Integer newStatus;
    private String rejectReason;

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getContentId() { return contentId; }
    public void setContentId(Long contentId) { this.contentId = contentId; }

    public List<Long> getContentIds() { return contentIds; }
    public void setContentIds(List<Long> contentIds) { this.contentIds = contentIds; }

    public Integer getNewStatus() { return newStatus; }
    public void setNewStatus(Integer newStatus) { this.newStatus = newStatus; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
