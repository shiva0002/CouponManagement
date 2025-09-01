package com.manage.Coupons.model;

import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("BXGY")
public class BxGyCoupon extends Coupon {
    
    private List<String> buyProducts;
    
    @Min(1)
    private Integer buyQuantity;
    
    private List<String> getProducts;
    
    @Min(1)
    private Integer getQuantity;
    
    @Min(1)
    private Integer repetitionLimit = 1;
    
}
