package com.manage.Coupons.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.manage.Coupons.dto.ApplicableCouponResponse;
import com.manage.Coupons.dto.CouponDTO;
import com.manage.Coupons.exception.ConstraintViolationException;
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

@Service
public class CouponService {
    
    @Autowired
    private CouponRepository couponRepository;
    
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
    
    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon with Id " + id + " Not Found"));
    }
    
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }
        try{
            return couponRepository.save(coupon);
        } catch (Exception e){
            throw new ConstraintViolationException("Could not save to Database");
        }
    }
    
    public Coupon updateCoupon(Long id, Coupon couponDetails) {
        Coupon coupon = couponRepository.findById(id)
            .orElseThrow(() -> new CouponNotFoundException("Coupon with Id " + id + " Not Found"));
        System.out.println(coupon);
        // Update fields
        coupon.setName(couponDetails.getName());
        coupon.setDescription(couponDetails.getDescription());
        coupon.setValidFrom(couponDetails.getValidFrom());
        coupon.setValidTo(couponDetails.getValidTo());
        coupon.setActive(couponDetails.isActive());
        
        return couponRepository.save(coupon);
    }
    
    public void deleteCoupon(Long id) {
        couponRepository.findById(id)
            .orElseThrow(() -> new CouponNotFoundException("No Coupon with id " + id + " found to delete"));
        couponRepository.deleteById(id);
    }
    
    public List<ApplicableCouponResponse> getApplicableCoupons(Cart cart) {
        List<Coupon> activeCoupons = couponRepository.findActiveCoupons(LocalDateTime.now());
        List<ApplicableCouponResponse> applicableCoupons = new ArrayList<>();
        
        for (Coupon coupon : activeCoupons) {
            ApplicableCouponResponse response = checkCouponApplicability(coupon, cart);
            if (response.getIsCouponApplicable()) {
                applicableCoupons.add(response);
            }
        }
        
        return applicableCoupons;
    }
    
    public Cart applyCoupon(Long couponId, Cart cart) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new CouponNotFoundException("Coupon with Id " + couponId + " Not Found"));
        ApplicableCouponResponse applicability = checkCouponApplicability(coupon, cart);
        if (!applicability.getIsCouponApplicable()) {
            throw new CouponNotApplicable("Coupon not applicable: " + applicability.getMessage());
        }
        
        return applyCouponToCart(coupon, cart);
    }
    
    private ApplicableCouponResponse checkCouponApplicability(Coupon coupon, Cart cart) {
        ApplicableCouponResponse response = new ApplicableCouponResponse();
        response.setCoupon(convertToDTO(coupon));
        try {
            boolean discount = false;
            String message = "Applicable";
            Boolean isCouponApplicable = true;
            
            if (coupon instanceof CartWiseCoupon cartWise) {
                discount = isCartWiseDiscountValid(cartWise, cart);
                if (!discount) {
                    message = "Cart total doesn't meet minimum requirement";
                    isCouponApplicable = false;
                }
            } else if (coupon instanceof ProductWiseCoupon productWise) {
                discount = isProductWiseDiscountValid(productWise, cart);
                if (!discount) {
                    message = "No applicable products in cart";
                    isCouponApplicable = false;
                }
            } else if (coupon instanceof BxGyCoupon bxgy) {
                discount = isBxGyDiscountValid(bxgy, cart);
                if (!discount) {
                    message = "Buy conditions not met";
                    isCouponApplicable = false;
                }
            }
            response.setMessage(message);
            response.setIsCouponApplicable(isCouponApplicable);
            
        } catch (Exception e) {
            response.setMessage("Error checking applicability: " + e.getMessage());
            response.setIsCouponApplicable(false);
        }
        
        return response;
    }
    
    private boolean isCartWiseDiscountValid(CartWiseCoupon coupon, Cart cart) {
        if (cart.getTotalAmount() < coupon.getMinCartAmount()) {
            return false;
        }

        return true;
    }
    
    private boolean isProductWiseDiscountValid(ProductWiseCoupon coupon, Cart cart) {
        int countProducts = 0;
        for (CartItem item : cart.getItems()) {
            if (coupon.getApplicableProducts().contains(item.getProductId())) {
                countProducts += item.getQuantity();
            }
        }
        
        if(countProducts > 0) return true;

        return false;
    }
    
    private boolean isBxGyDiscountValid(BxGyCoupon coupon, Cart cart) {
        // Count total buy products in cart
        long totalBuyProducts = cart.getItems().stream()
            .filter(item -> coupon.getBuyProducts().contains(item.getProductId()))
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        boolean isApplicable = false;

        if(totalBuyProducts >= coupon.getBuyQuantity()) isApplicable = true;

        return isApplicable;
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
        double discount = 0;

        if (coupon.getDiscountPercentage() != null) {
            discount = cart.getTotalAmount() * (coupon.getDiscountPercentage() / 100);
        } 
        
        if (coupon.getFixedDiscount() != null) {
            discount = Math.min(coupon.getFixedDiscount(), cart.getTotalAmount());
        }

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
        // Fix: if Item 1 is less than get Quantity
        // for (CartItem item : cart.getItems()) {
        //     if (coupon.getGetProducts().contains(item.getProductId()) && applicableTimes > 0) {
        //         int freeItems = Math.min(item.getQuantity(), applicableTimes * coupon.getGetQuantity());
        //         double itemDiscount = item.getPrice() * freeItems;
        //         item.setDiscountedPrice((item.getPrice() * item.getQuantity() - itemDiscount) / item.getQuantity());
        //         applicableTimes -= freeItems >= coupon.getGetQuantity() ? freeItems / coupon.getGetQuantity() : freeItems;
        //     }
        // }
        
        // Find totalFreeItems
        int totalFreeItems = applicableTimes * coupon.getGetQuantity();
        
        // Find total Get Products in the cart
        int totalGetProducts = cart.getItems().stream()
            .filter(item -> coupon.getGetProducts().contains(item.getProductId()))
            .mapToInt(CartItem::getQuantity)
            .sum();
        
        // Find total items to pay for
        int totalPayItems = totalFreeItems >= totalGetProducts ? 0 : totalGetProducts - totalFreeItems;

        // Find the discount price for each item after amount has been paid for payable items 
        for(CartItem item: cart.getItems()){
            if(coupon.getGetProducts().contains(item.getProductId()) && totalPayItems >=0 ){
                int payableItemsCount = Math.min(item.getQuantity(),totalPayItems);
                double payablePrice = payableItemsCount * item.getPrice();
                item.setDiscountedPrice(payablePrice/item.getQuantity());
                totalPayItems -= payableItemsCount;
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

    public List<Coupon> getActiveCoupons(){
        List<Coupon> activeCoupons = couponRepository.findActiveCoupons(LocalDateTime.now());

        return activeCoupons;
    }
}