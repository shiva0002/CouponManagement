package com.manage.Coupons.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.manage.Coupons.model.CouponType;

import lombok.Data;

@Data
public class CouponDTO {
    private Long id;
    private String name;
    private String code;
    private CouponType type;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean active;
    
    // Cart-wise specific
    private Double minCartAmount;
    private Double discountPercentage;
    private Double fixedDiscount;
    
    // Product-wise specific
    private List<String> applicableProducts;
    
    // BxGy specific
    private List<String> buyProducts;
    private Integer buyQuantity;
    private List<String> getProducts;
    private Integer getQuantity;
    private Integer repetitionLimit;
    
}
