package com.manage.Coupons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.manage.Coupons.dto.ApplicableCouponResponse;
import com.manage.Coupons.dto.ApplyCouponRequest;
import com.manage.Coupons.exception.CouponNotFoundException;
import com.manage.Coupons.model.Cart;
import com.manage.Coupons.model.Coupon;
import com.manage.Coupons.service.CouponService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coupons")
@Slf4j
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    @PostMapping
    public ResponseEntity<Object> createCoupon(@Valid @RequestBody Coupon coupon) {
        try{
            Coupon createdCoupon = couponService.createCoupon(coupon);
            return ResponseEntity.ok(createdCoupon);
        } catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        Optional<Coupon> coupon = couponService.getCouponById(id);
        return coupon.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id, 
                                              @Valid @RequestBody Coupon couponDetails) {
        try {
            Coupon updatedCoupon = couponService.updateCoupon(id, couponDetails);
            return ResponseEntity.ok(updatedCoupon);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        try {
            couponService.deleteCoupon(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/applicable-coupons")
    public ResponseEntity<List<ApplicableCouponResponse>> getApplicableCoupons(@RequestBody Cart cart) {
        List<ApplicableCouponResponse> applicableCoupons = couponService.getApplicableCoupons(cart);
        return ResponseEntity.ok(applicableCoupons);
    }
    
    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<?> applyCoupon(@PathVariable Long id,
                                           @RequestBody ApplyCouponRequest request) {
        try {
            Cart cart = new Cart();
            cart.setId(request.getCartId());
            cart.setItems(request.getItems());
            cart.setTotalAmount(couponService.calculateTotalAmount(request.getItems()));
            
            Cart updatedCart = couponService.applyCoupon(id, cart);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) { // Global Exception Handling
            if(e instanceof CouponNotFoundException){
                log.info("Coupon not found: " + e.getMessage());
                return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(e.getMessage());
            } else {
                log.info("Error applying coupon: " + e.getMessage());
                return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(e.getMessage());
            }
        }
    }

    @GetMapping("/active-coupons")
    public ResponseEntity<List<Coupon>> getActiveCoupons(){
        List<Coupon> activeCoupons = couponService.getActiveCoupons();
        return ResponseEntity.ok().body(activeCoupons);
    }
}
