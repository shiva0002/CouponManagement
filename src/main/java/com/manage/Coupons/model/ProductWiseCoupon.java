package com.manage.Coupons.model;

import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("PRODUCT_WISE")
public class ProductWiseCoupon extends Coupon {
    
    private List<String> applicableProducts;
    
    @Min(0)
    @Max(100)
    private Double discountPercentage;
    
    @Min(0)
    private Double fixedDiscount;
    
}
