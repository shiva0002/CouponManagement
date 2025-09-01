package com.manage.Coupons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.manage.Coupons.model.Coupon;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    
    @Query("SELECT c FROM Coupon c WHERE c.active = true AND " +
           "(:currentTime BETWEEN c.validFrom AND c.validTo OR c.validFrom IS NULL)")
    List<Coupon> findActiveCoupons(@Param("currentTime") LocalDateTime currentTime);
    
    List<Coupon> findByType(String type);
    
    boolean existsByCode(String code);
}