package com.manage.Coupons.model;

import java.util.List;

import lombok.Data;

@Data
public class CartItem {
    private String productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double discountedPrice;
}
