package com.manage.Coupons.dto;

import lombok.Data;

@Data
public class ApplicableCouponResponse {
    private CouponDTO coupon;
    private String message;
    private Boolean isCouponApplicable;
}
