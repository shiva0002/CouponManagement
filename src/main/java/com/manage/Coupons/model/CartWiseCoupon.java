package com.manage.Coupons.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("CART_WISE")
public class CartWiseCoupon extends Coupon {
    @Min(0)
    private Double minCartAmount;
    
    @Min(0)
    @Max(100)
    private Double discountPercentage;
    
    @Min(0)
    private Double fixedDiscount;
    
}
