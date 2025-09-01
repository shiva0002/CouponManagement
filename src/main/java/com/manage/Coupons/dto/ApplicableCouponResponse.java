package com.manage.Coupons.dto;

import lombok.Data;

@Data
public class ApplicableCouponResponse {
    private CouponDTO coupon;
    private Double discountAmount;
    private String message;
}
