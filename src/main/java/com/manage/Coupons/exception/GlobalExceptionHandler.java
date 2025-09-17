package com.manage.Coupons.exception;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.manage.Coupons.error.ErrorInfo;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException{
    
    @ExceptionHandler(CouponNotFoundException.class)
    public ResponseEntity<ErrorInfo> couponNotFoundExceptionHandler(CouponNotFoundException exception){
        ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setStatus(HttpStatus.NOT_FOUND);
            errorInfo.setDate(LocalDate.now());
            errorInfo.setMessage(exception.getMessage());
            errorInfo.setDetails("Coupon Not Available");

        return new ResponseEntity<>(errorInfo,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CouponNotApplicable.class)
    public ResponseEntity<ErrorInfo> couponNotApplicableExceptionHandler(CouponNotApplicable exception){
        ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setStatus(HttpStatus.NOT_FOUND);
            errorInfo.setDate(LocalDate.now());
            errorInfo.setMessage(exception.getMessage());
            errorInfo.setDetails("Coupon Not Applicable");

        return new ResponseEntity<>(errorInfo,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorInfo> constraintViolationExceptionHandler(ConstraintViolationException exception){
        ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setStatus(HttpStatus.BAD_REQUEST);
            errorInfo.setDate(LocalDate.now());
            errorInfo.setMessage(exception.getMessage());
            errorInfo.setDetails("Error in forming Request Body");

        return new ResponseEntity<>(errorInfo,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> validationCheckExceptionHandler(MethodArgumentNotValidException exception) {
        ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setStatus(HttpStatus.BAD_REQUEST);
            errorInfo.setDate(LocalDate.now());
            errorInfo.setMessage(exception.getMessage());
            errorInfo.setDetails("Constraints Not Fulfilled");

        return new ResponseEntity<>(errorInfo,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorInfo> ExceptionHandler(Exception exception) {
        ErrorInfo errorInfo = new ErrorInfo();
            errorInfo.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            errorInfo.setDate(LocalDate.now());
            errorInfo.setMessage(exception.getMessage());
            errorInfo.setDetails("Something went wrong...");

        return new ResponseEntity<>(errorInfo,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
