package com.manage.Coupons.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Entity
@Data
@Table(name = "coupons")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "coupon_type")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CartWiseCoupon.class, name = "CART_WISE"),
    @JsonSubTypes.Type(value = ProductWiseCoupon.class, name = "PRODUCT_WISE"),
    @JsonSubTypes.Type(value = BxGyCoupon.class, name = "BXGY")
})
public abstract class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String code;
    
    @Enumerated(EnumType.STRING)
    private CouponType type;
    
    private String description;
    
    private LocalDateTime validFrom;
    
    private LocalDateTime validTo;
    
    private boolean active = true;
    
    @Column(name = "coupon_type", insertable = false, updatable = false)
    private String couponType;
    
}







