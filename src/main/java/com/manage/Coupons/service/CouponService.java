package com.manage.Coupons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manage.Coupons.dto.ApplicableCouponResponse;
import com.manage.Coupons.dto.CouponDTO;
import com.manage.Coupons.exception.CouponNotApplicable;
import com.manage.Coupons.exception.CouponNotFoundException;
import com.manage.Coupons.model.BxGyCoupon;
import com.manage.Coupons.model.Cart;
import com.manage.Coupons.model.CartItem;
import com.manage.Coupons.model.CartWiseCoupon;
import com.manage.Coupons.model.Coupon;
import com.manage.Coupons.model.ProductWiseCoupon;
import com.manage.Coupons.repository.CouponRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {
    
    @Autowired
    private CouponRepository couponRepository;
    
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
    
    public Optional<Coupon> getCouponById(Long id) {
        return couponRepository.findById(id);
    }
    
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        return couponRepository.save(coupon);
    }
    
    public Coupon updateCoupon(Long id, Coupon couponDetails) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));
        
        // Update fields
        coupon.setName(couponDetails.getName());
        coupon.setDescription(couponDetails.getDescription());
        coupon.setValidFrom(couponDetails.getValidFrom());
        coupon.setValidTo(couponDetails.getValidTo());
        coupon.setActive(couponDetails.isActive());
        
        return couponRepository.save(coupon);
    }
    
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
    
    public List<ApplicableCouponResponse> getApplicableCoupons(Cart cart) {
        List<Coupon> activeCoupons = couponRepository.findActiveCoupons(LocalDateTime.now());
        List<ApplicableCouponResponse> applicableCoupons = new ArrayList<>();
        
        for (Coupon coupon : activeCoupons) {
            ApplicableCouponResponse response = checkCouponApplicability(coupon, cart);
            if (response.getDiscountAmount() > 0) {
                applicableCoupons.add(response);
            }
        }
        
        return applicableCoupons;
    }
    
    public Cart applyCoupon(Long couponId, Cart cart) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException("Coupon not found"));
        ApplicableCouponResponse applicability = checkCouponApplicability(coupon, cart);
        if (applicability.getDiscountAmount() <= 0) {
            throw new CouponNotApplicable("Coupon not applicable: " + applicability.getMessage());
        }
        
        return applyCouponToCart(coupon, cart);
    }
    
    private ApplicableCouponResponse checkCouponApplicability(Coupon coupon, Cart cart) {
        ApplicableCouponResponse response = new ApplicableCouponResponse();
        response.setCoupon(convertToDTO(coupon));
        try {
            double discount = 0;
            String message = "Applicable";
            
            if (coupon instanceof CartWiseCoupon cartWise) {
                discount = calculateCartWiseDiscount(cartWise, cart);
                if (discount == 0) {
                    message = "Cart total doesn't meet minimum requirement";
                }
            } else if (coupon instanceof ProductWiseCoupon productWise) {
                discount = calculateProductWiseDiscount(productWise, cart);
                if (discount == 0) {
                    message = "No applicable products in cart";
                }
            } else if (coupon instanceof BxGyCoupon bxgy) {
                discount = calculateBxGyDiscount(bxgy, cart);
                if (discount == 0) {
                    message = "Buy conditions not met";
                }
            }
            
            response.setDiscountAmount(discount);
            response.setMessage(message);
            
        } catch (Exception e) {
            response.setDiscountAmount(0.0);
            response.setMessage("Error checking applicability: " + e.getMessage());
        }
        
        return response;
    }
    
    private double calculateCartWiseDiscount(CartWiseCoupon coupon, Cart cart) {
        if (cart.getTotalAmount() < coupon.getMinCartAmount()) {
            return 0;
        }
        
        if (coupon.getDiscountPercentage() != null) {
            return cart.getTotalAmount() * (coupon.getDiscountPercentage() / 100);
        } else if (coupon.getFixedDiscount() != null) {
            return Math.min(coupon.getFixedDiscount(), cart.getTotalAmount());
        }
        
        return 0;
    }
    
    private double calculateProductWiseDiscount(ProductWiseCoupon coupon, Cart cart) {
        double totalDiscount = 0;
        
        for (CartItem item : cart.getItems()) {
            if (coupon.getApplicableProducts().contains(item.getProductId())) {
                if (coupon.getDiscountPercentage() != null) {
                    totalDiscount += item.getPrice() * item.getQuantity() * (coupon.getDiscountPercentage() / 100);
                } else if (coupon.getFixedDiscount() != null) {
                    totalDiscount += coupon.getFixedDiscount() * item.getQuantity();
                }
            }
        }
        
        return totalDiscount;
    }
    
    private double calculateBxGyDiscount(BxGyCoupon coupon, Cart cart) {
        // Count total buy products in cart
        long totalBuyProducts = cart.getItems().stream()
            .filter(item -> coupon.getBuyProducts().contains(item.getProductId()))
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        // Count total get products in cart
        long totalGetProducts = cart.getItems().stream()
            .filter(item -> coupon.getGetProducts().contains(item.getProductId()))
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        // Calculate how many times the coupon can be applied
        int applicableTimes = (int) Math.min(
            totalBuyProducts / coupon.getBuyQuantity(),
            coupon.getRepetitionLimit()
        );
        
        // Calculate potential free items value
        double freeItemsValue = 0;
        for (CartItem item : cart.getItems()) {
            if (coupon.getGetProducts().contains(item.getProductId())) {
                freeItemsValue += item.getPrice();
            }
        }
        
        // Return the maximum discount that can be applied
        return Math.min(freeItemsValue, applicableTimes * getAverageGetProductPrice(cart, coupon));
    }
    
    private double getAverageGetProductPrice(Cart cart, BxGyCoupon coupon) {
        double totalPrice = 0;
        int count = 0;
        
        for (CartItem item : cart.getItems()) {
            if (coupon.getGetProducts().contains(item.getProductId())) {
                totalPrice += item.getPrice() * item.getQuantity();
                count += item.getQuantity();
            }
        }
        
        return count > 0 ? totalPrice / count : 0;
    }
    
    private Cart applyCouponToCart(Coupon coupon, Cart cart) {
        Cart updatedCart = new Cart();
        updatedCart.setId(cart.getId());
        updatedCart.setItems(new ArrayList<>(cart.getItems()));
        updatedCart.setTotalAmount(cart.getTotalAmount());
        
        if (coupon instanceof CartWiseCoupon cartWise) {
            applyCartWiseCoupon(cartWise, updatedCart);
        } else if (coupon instanceof ProductWiseCoupon productWise) {
            applyProductWiseCoupon(productWise, updatedCart);
        } else if (coupon instanceof BxGyCoupon bxgy) {
            applyBxGyCoupon(bxgy, updatedCart);
        }
        
        return updatedCart;
    }
    
    private void applyCartWiseCoupon(CartWiseCoupon coupon, Cart cart) {
        double discount = calculateCartWiseDiscount(coupon, cart);
        double discountPerItem = discount / cart.getTotalAmount();
        
        for (CartItem item : cart.getItems()) {
            double itemDiscount = item.getPrice() * item.getQuantity() * discountPerItem;
            item.setDiscountedPrice(item.getPrice() - (itemDiscount / item.getQuantity()));
        }
        cart.setTotalAmount(cart.getTotalAmount() - discount);
    }
    
    private void applyProductWiseCoupon(ProductWiseCoupon coupon, Cart cart) {
        for (CartItem item : cart.getItems()) {
            if (coupon.getApplicableProducts().contains(item.getProductId())) {
                if (coupon.getDiscountPercentage() != null) {
                    double discount = item.getPrice() * (coupon.getDiscountPercentage() / 100);
                    item.setDiscountedPrice(item.getPrice() - discount);
                } else if (coupon.getFixedDiscount() != null) {
                    item.setDiscountedPrice(Math.max(0, item.getPrice() - coupon.getFixedDiscount()));
                }
            }
        }
        cart.setTotalAmount(cart.getItems().stream()
            .mapToDouble(item -> item.getDiscountedPrice() * item.getQuantity())
            .sum());
    }

    public Double calculateTotalAmount(List<CartItem> items) {
        return items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
    }
    
    private void applyBxGyCoupon(BxGyCoupon coupon, Cart cart) {
        long totalBuyProducts = cart.getItems().stream()
            .filter(item -> coupon.getBuyProducts().contains(item.getProductId()))
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        int applicableTimes = (int) Math.min(
            totalBuyProducts / coupon.getBuyQuantity(),
            coupon.getRepetitionLimit()
        );
        
        if (applicableTimes <= 0) {
            return;
        }
        
        for (CartItem item : cart.getItems()) {
            if (coupon.getGetProducts().contains(item.getProductId()) && applicableTimes > 0) {
                int freeItems = Math.min(item.getQuantity(), applicableTimes * coupon.getGetQuantity());
                double itemDiscount = item.getPrice() * freeItems;
                item.setDiscountedPrice((item.getPrice() * item.getQuantity() - itemDiscount) / item.getQuantity());
                applicableTimes -= freeItems / coupon.getGetQuantity();
            }
        }
        
        cart.setTotalAmount(cart.getItems().stream()
            .mapToDouble(item -> {
                if(item.getDiscountedPrice() == null) {
                    item.setDiscountedPrice(item.getPrice());
                }
                return item.getDiscountedPrice() * item.getQuantity();
            })
            .sum());
    }
    
    private CouponDTO convertToDTO(Coupon coupon) {
        // Conversion logic from entity to DTO
        CouponDTO couponDTO = new CouponDTO();
        couponDTO.setId(coupon.getId());
        couponDTO.setName(coupon.getName());
        couponDTO.setCode(coupon.getCode());
        couponDTO.setType(coupon.getType());
        couponDTO.setDescription(coupon.getDescription());
        couponDTO.setValidFrom(coupon.getValidFrom());
        couponDTO.setValidTo(coupon.getValidTo());
        couponDTO.setActive(coupon.isActive());
        if (coupon instanceof CartWiseCoupon cartWise) {
            couponDTO.setMinCartAmount(cartWise.getMinCartAmount());
            couponDTO.setDiscountPercentage(cartWise.getDiscountPercentage());
            couponDTO.setFixedDiscount(cartWise.getFixedDiscount());
        } else if (coupon instanceof ProductWiseCoupon productWise) {
            couponDTO.setApplicableProducts(productWise.getApplicableProducts());
            couponDTO.setDiscountPercentage(productWise.getDiscountPercentage());
            couponDTO.setFixedDiscount(productWise.getFixedDiscount());
        } else if (coupon instanceof BxGyCoupon bxgy) {
            couponDTO.setBuyProducts(bxgy.getBuyProducts());
            couponDTO.setBuyQuantity(bxgy.getBuyQuantity());
            couponDTO.setGetProducts(bxgy.getGetProducts());
            couponDTO.setGetQuantity(bxgy.getGetQuantity());
            couponDTO.setRepetitionLimit(bxgy.getRepetitionLimit());
        }
        return couponDTO;
    }
}