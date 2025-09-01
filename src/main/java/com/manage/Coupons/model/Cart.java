package com.manage.Coupons.model;

import java.util.List;

import lombok.Data;

@Data
public class Cart {
    private String id;
    private List<CartItem> items;
    private Double totalAmount;
}
