package com.manage.Coupons.service;

import com.manage.Coupons.dto.ApplicableCouponResponse;
import com.manage.Coupons.dto.CouponDTO;
import com.manage.Coupons.exception.CouponNotApplicable;
import com.manage.Coupons.exception.CouponNotFoundException;
import com.manage.Coupons.model.BxGyCoupon;
import com.manage.Coupons.model.Cart;
import com.manage.Coupons.model.CartItem;
import com.manage.Coupons.model.CartWiseCoupon;
import com.manage.Coupons.model.Coupon;
import com.manage.Coupons.model.CouponType;
import com.manage.Coupons.model.ProductWiseCoupon;
import com.manage.Coupons.repository.CouponRepository;
import com.manage.Coupons.service.CouponService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Cart createCart(double totalAmount, List<CartItem> items) {
        Cart cart = new Cart();
        cart.setId("1");
        cart.setTotalAmount(totalAmount);
        cart.setItems(items);
        return cart;
    }

    private CartItem createCartItem(String productId, double price, int quantity) {
        CartItem item = new CartItem();
        item.setProductId(productId);
        item.setPrice(price);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void testGetAllCoupons() {
        List<Coupon> coupons = List.of(new CartWiseCoupon(), new ProductWiseCoupon());
        when(couponRepository.findAll()).thenReturn(coupons);

        List<Coupon> result = couponService.getAllCoupons();

        assertEquals(2, result.size());
        verify(couponRepository).findAll();
    }

    @Test
    void testGetCouponById_Found() {
        Coupon coupon = new CartWiseCoupon();
        coupon.setId(1L);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        Optional<Coupon> result = couponService.getCouponById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testGetCouponById_NotFound() {
        when(couponRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Coupon> result = couponService.getCouponById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateCoupon_Success() {
        Coupon coupon = new CartWiseCoupon();
        coupon.setCode("CODE1");
        when(couponRepository.existsByCode("CODE1")).thenReturn(false);
        when(couponRepository.save(coupon)).thenReturn(coupon);

        Coupon result = couponService.createCoupon(coupon);

        assertEquals(coupon, result);
        verify(couponRepository).save(coupon);
    }

    @Test
    void testCreateCoupon_DuplicateCode() {
        Coupon coupon = new CartWiseCoupon();
        coupon.setCode("CODE1");
        when(couponRepository.existsByCode("CODE1")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> couponService.createCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_Success() {
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setId(1L);
        coupon.setName("Old");
        CartWiseCoupon details = new CartWiseCoupon();
        details.setName("New");
        details.setDescription("desc");
        details.setValidFrom(LocalDateTime.now());
        details.setValidTo(LocalDateTime.now().plusDays(1));
        details.setActive(true);

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any())).thenReturn(coupon);

        Coupon result = couponService.updateCoupon(1L, details);

        assertEquals("New", result.getName());
        verify(couponRepository).save(coupon);
    }

    @Test
    void testUpdateCoupon_NotFound() {
        when(couponRepository.findById(2L)).thenReturn(Optional.empty());
        CartWiseCoupon details = new CartWiseCoupon();

        assertThrows(CouponNotFoundException.class, () -> couponService.updateCoupon(2L, details));
    }

    @Test
    void testDeleteCoupon() {
        couponService.deleteCoupon(1L);
        verify(couponRepository).deleteById(1L);
    }

    @Test
    void testGetApplicableCoupons() {
        Cart cart = createCart(200, List.of(createCartItem("1", 100, 2)));
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setMinCartAmount(100.0);
        coupon.setDiscountPercentage(10.0);
        coupon.setId(1L);
        coupon.setCode("CODE1");
        coupon.setName("CartWise");
        coupon.setType(CouponType.CART_WISE);
        coupon.setDescription("desc");
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);

        when(couponRepository.findActiveCoupons(any())).thenReturn(List.of(coupon));

        List<ApplicableCouponResponse> responses = couponService.getApplicableCoupons(cart);

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getDiscountAmount() > 0);
    }

    @Test
    void testApplyCoupon_Success() {
        Cart cart = createCart(200, List.of(createCartItem("1", 100, 2)));
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setId(1L);
        coupon.setMinCartAmount(100.0);
        coupon.setDiscountPercentage(10.0);

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        Cart updatedCart = couponService.applyCoupon(1L, cart);

        assertNotNull(updatedCart);
        assertTrue(updatedCart.getTotalAmount() < cart.getTotalAmount());
    }

    @Test
    void testApplyCoupon_NotApplicable() {
        Cart cart = createCart(50, List.of(createCartItem("1", 50, 1)));
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setId(1L);
        coupon.setMinCartAmount(100.0);
        coupon.setDiscountPercentage(10.0);

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        assertThrows(CouponNotApplicable.class, () -> couponService.applyCoupon(1L, cart));
    }

    @Test
    void testCalculateCartWiseDiscount_Percentage() throws Exception {
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setMinCartAmount(100.0);
        coupon.setDiscountPercentage(10.0);
        Cart cart = createCart(200, List.of(createCartItem("1", 100, 2)));

        Method method = CouponService.class.getDeclaredMethod("calculateCartWiseDiscount", CartWiseCoupon.class, Cart.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        double discount = (double) method.invoke(couponService, coupon, cart);

        assertEquals(20.0, discount);
    }

    @Test
    void testCalculateCartWiseDiscount_Fixed() throws Exception {
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setMinCartAmount(100.0);
        coupon.setFixedDiscount(30.0);
        Cart cart = createCart(200, List.of(createCartItem("1", 100, 2)));

        Method method = CouponService.class.getDeclaredMethod("calculateCartWiseDiscount", CartWiseCoupon.class, Cart.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        double discount = (double) method.invoke(couponService, coupon, cart);

        assertEquals(30.0, discount);
    }

    @Test
    void testCalculateProductWiseDiscount_Percentage() throws Exception {
        ProductWiseCoupon coupon = new ProductWiseCoupon();
        coupon.setApplicableProducts(List.of("P001"));
        coupon.setDiscountPercentage(10.0);
        Cart cart = createCart(200, List.of(createCartItem("P001", 100, 2), createCartItem("P002", 50, 1)));

        Method method = CouponService.class.getDeclaredMethod("calculateProductWiseDiscount", ProductWiseCoupon.class, Cart.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        double discount = (double) method.invoke(couponService, coupon, cart);

        assertEquals(20.0, discount);
    }

    @Test
    void testCalculateProductWiseDiscount_Fixed() throws Exception {
        ProductWiseCoupon coupon = new ProductWiseCoupon();
        coupon.setApplicableProducts(List.of("P001"));
        coupon.setFixedDiscount(5.0);
        Cart cart = createCart(200, List.of(createCartItem("P001", 100, 2), createCartItem("P002", 50, 1)));

        Method method = CouponService.class.getDeclaredMethod("calculateProductWiseDiscount", ProductWiseCoupon.class, Cart.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        double discount = (double) method.invoke(couponService, coupon, cart);

        assertEquals(10.0, discount);
    }

    @Test
    void testCalculateBxGyDiscount() throws Exception {
        BxGyCoupon coupon = new BxGyCoupon();
        coupon.setBuyProducts(List.of("P001"));
        coupon.setBuyQuantity(2);
        coupon.setGetProducts(List.of("P002"));
        coupon.setGetQuantity(1);
        coupon.setRepetitionLimit(1);
        Cart cart = createCart(250, List.of(createCartItem("P001", 100, 2), createCartItem("P002", 50, 1)));

        Method method = CouponService.class.getDeclaredMethod("calculateBxGyDiscount", BxGyCoupon.class, Cart.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        double discount = (double) method.invoke(couponService, coupon, cart);

        assertTrue(discount > 0);
    }

    @Test
    void testCalculateTotalAmount() {
        List<CartItem> items = List.of(createCartItem("1", 100, 2), createCartItem("2", 50, 1));
        double total = couponService.calculateTotalAmount(items);
        assertEquals(250.0, total);
    }

    @Test
    void testConvertToDTO_CartWise() throws Exception {
        CartWiseCoupon coupon = new CartWiseCoupon();
        coupon.setId(1L);
        coupon.setName("CartWise");
        coupon.setCode("CODE1");
        coupon.setType(CouponType.CART_WISE);
        coupon.setDescription("desc");
        coupon.setValidFrom(LocalDateTime.now());
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);
        coupon.setMinCartAmount(100.0);
        coupon.setDiscountPercentage(10.0);

        Method method = CouponService.class.getDeclaredMethod("convertToDTO", Coupon.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        CouponDTO dto = (CouponDTO) method.invoke(couponService, coupon);

        assertEquals("CartWise", dto.getName());
        assertEquals(100.0, dto.getMinCartAmount());
        assertEquals(10.0, dto.getDiscountPercentage());
    }

    @Test
    void testConvertToDTO_ProductWise() throws Exception {
        ProductWiseCoupon coupon = new ProductWiseCoupon();
        coupon.setId(2L);
        coupon.setName("ProductWise");
        coupon.setCode("CODE2");
        coupon.setType(CouponType.PRODUCT_WISE);
        coupon.setDescription("desc");
        coupon.setValidFrom(LocalDateTime.now());
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);
        coupon.setApplicableProducts(List.of("P001", "P002"));
        coupon.setDiscountPercentage(15.0);

        Method method = CouponService.class.getDeclaredMethod("convertToDTO", Coupon.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        CouponDTO dto = (CouponDTO) method.invoke(couponService, coupon);

        assertEquals("ProductWise", dto.getName());
        assertEquals(List.of("P001", "P002"), dto.getApplicableProducts());
        assertEquals(15.0, dto.getDiscountPercentage());
    }

    @Test
    void testConvertToDTO_BxGy() throws Exception {
        BxGyCoupon coupon = new BxGyCoupon();
        coupon.setId(3L);
        coupon.setName("BxGy");
        coupon.setCode("CODE3");
        coupon.setType(CouponType.BXGY);
        coupon.setDescription("desc");
        coupon.setValidFrom(LocalDateTime.now());
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        coupon.setActive(true);
        coupon.setBuyProducts(List.of("P001"));
        coupon.setBuyQuantity(2);
        coupon.setGetProducts(List.of("P002"));
        coupon.setGetQuantity(1);
        coupon.setRepetitionLimit(2);

        Method method = CouponService.class.getDeclaredMethod("convertToDTO", Coupon.class);
        method.setAccessible(true);

        // Act
        CouponService couponService = new CouponService();
        CouponDTO dto = (CouponDTO) method.invoke(couponService, coupon);

        assertEquals("BxGy", dto.getName());
        assertEquals(List.of("P001"), dto.getBuyProducts());
        assertEquals(2, dto.getBuyQuantity());
        assertEquals(List.of("P002"), dto.getGetProducts());
        assertEquals(1, dto.getGetQuantity());
        assertEquals(2, dto.getRepetitionLimit());
    }
}