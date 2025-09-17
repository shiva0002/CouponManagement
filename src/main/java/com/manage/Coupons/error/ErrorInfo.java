package com.manage.Coupons.error;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorInfo {
    
    private HttpStatus status;
    private LocalDate date;
    private String message;
    private String details;

}
