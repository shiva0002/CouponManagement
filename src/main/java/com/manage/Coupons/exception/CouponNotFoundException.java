package com.manage.Coupons.exception;

public class CouponNotFoundException extends RuntimeException{
    public CouponNotFoundException(String message) {
        super(message);
    }
}
