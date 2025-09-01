package com.manage.Coupons.dto;

import java.util.List;

import com.manage.Coupons.model.CartItem;

import lombok.Data;

@Data
public class ApplyCouponRequest {
    private String cartId;
    private List<CartItem> items;
}
