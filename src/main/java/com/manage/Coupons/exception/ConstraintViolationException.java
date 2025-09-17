package com.manage.Coupons.exception;

public class ConstraintViolationException extends RuntimeException{

    public ConstraintViolationException(String message){
        super(message);
    }
}
