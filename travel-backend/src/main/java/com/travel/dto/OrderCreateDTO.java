package com.travel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderCreateDTO {
    @NotNull(message = "线路ID不能为空")
    private Long lineId;
    
    @NotBlank(message = "出发日期不能为空")
    private String departureDate;
    
    @NotEmpty(message = "出行人信息不能为空")
    private List<TravelerDTO> travelers;
    
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;
    
    @NotBlank(message = "联系人电话不能为空")
    private String contactPhone;
    
    private String remark;
    private String payType;
    private Long couponId;

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
    
    public static class TravelerDTO {
        @NotBlank(message = "姓名不能为空")
        private String name;
        @NotBlank(message = "身份证不能为空")
        private String idCard;
        @NotBlank(message = "手机号不能为空")
        private String phone;
        
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
    
    public Long getLineId() {
        return lineId;
    }

    public void setLineId(Long lineId) {
        this.lineId = lineId;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public List<TravelerDTO> getTravelers() {
        return travelers;
    }

    public void setTravelers(List<TravelerDTO> travelers) {
        this.travelers = travelers;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }
}
