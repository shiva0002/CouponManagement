package com.manage.Coupons.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.manage.Coupons.dto.ApplicableCouponResponse;
import com.manage.Coupons.dto.ApplyCouponRequest;
import com.manage.Coupons.model.Cart;
import com.manage.Coupons.model.Coupon;
import com.manage.Coupons.service.CouponService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@Slf4j
public class CouponController {

    @Autowired
    private CouponService couponService;

    @PostMapping
    public ResponseEntity<?> createCoupon(@Valid @RequestBody Coupon coupon) {
        Coupon createdCoupon = couponService.createCoupon(coupon);
        return ResponseEntity.ok(createdCoupon);
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return new ResponseEntity<Coupon>(coupon, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id,
            @Valid @RequestBody Coupon couponDetails) {
        Coupon updatedCoupon = couponService.updateCoupon(id, couponDetails);
        return ResponseEntity.ok(updatedCoupon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {

        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/applicable-coupons")
    public ResponseEntity<List<ApplicableCouponResponse>> getApplicableCoupons(@RequestBody Cart cart) {
        List<ApplicableCouponResponse> applicableCoupons = couponService.getApplicableCoupons(cart);
        return ResponseEntity.ok(applicableCoupons);
    }

    @PostMapping("/apply-coupon/{id}")
    public ResponseEntity<?> applyCoupon(@PathVariable Long id,
            @RequestBody ApplyCouponRequest request) {
        Cart cart = new Cart();
        cart.setId(request.getCartId());
        cart.setItems(request.getItems());
        cart.setTotalAmount(couponService.calculateTotalAmount(request.getItems()));

        Cart updatedCart = couponService.applyCoupon(id, cart);
        return ResponseEntity.ok(updatedCart);
    }

    @GetMapping("/active-coupons")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        List<Coupon> activeCoupons = couponService.getActiveCoupons();
        return ResponseEntity.ok().body(activeCoupons);
    }
}
