// package com.manage.Coupons.controller;

// import com.manage.Coupons.dto.ApplicableCouponResponse;
// import com.manage.Coupons.dto.ApplyCouponRequest;
// import com.manage.Coupons.exception.CouponNotFoundException;
// import com.manage.Coupons.model.Cart;
// import com.manage.Coupons.model.CartItem;
// import com.manage.Coupons.model.CartWiseCoupon;
// import com.manage.Coupons.model.Coupon;
// import com.manage.Coupons.model.ProductWiseCoupon;
// import com.manage.Coupons.service.CouponService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.*;
// import org.springframework.http.ResponseEntity;
// import java.util.*;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// class CouponControllerTest {

//     @Mock
//     private CouponService couponService;

//     @InjectMocks
//     private CouponController couponController;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     // Helper for concrete Coupon
//     private Coupon createCartWiseCoupon(Long id, String code) {
//         CartWiseCoupon coupon = new CartWiseCoupon();
//         coupon.setId(id);
//         coupon.setCode(code);
//         coupon.setName("CartWise");
//         coupon.setDescription("desc");
//         coupon.setActive(true);
//         coupon.setMinCartAmount(100.0);
//         coupon.setDiscountPercentage(10.0);
//         return coupon;
//     }

//     private Coupon createProductWiseCoupon(Long id, String code) {
//         ProductWiseCoupon coupon = new ProductWiseCoupon();
//         coupon.setId(id);
//         coupon.setCode(code);
//         coupon.setName("ProductWise");
//         coupon.setDescription("desc");
//         coupon.setActive(true);
//         coupon.setApplicableProducts(List.of("P1"));
//         coupon.setDiscountPercentage(15.0);
//         return coupon;
//     }

//     private Cart createCart(String id, double totalAmount, List<CartItem> items) {
//         Cart cart = new Cart();
//         cart.setId(id);
//         cart.setTotalAmount(totalAmount);
//         cart.setItems(items);
//         return cart;
//     }

//     private CartItem createCartItem(String productId, double price, int quantity) {
//         CartItem item = new CartItem();
//         item.setProductId(productId);
//         item.setPrice(price);
//         item.setQuantity(quantity);
//         return item;
//     }

//     @Test
//     void testCreateCartWiseCoupon() {
//         Coupon coupon = createCartWiseCoupon(1L, "CODE1");
//         when(couponService.createCoupon(any())).thenReturn(coupon);

//         ResponseEntity<Coupon> response = couponController.createCoupon(coupon);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupon, response.getBody());
//         verify(couponService).createCoupon(coupon);
//     }

//     @Test
//     void testCreateProductWiseCoupon() {
//         Coupon coupon = createProductWiseCoupon(2L, "CODE2");
//         when(couponService.createCoupon(any())).thenReturn(coupon);

//         ResponseEntity<Coupon> response = couponController.createCoupon(coupon);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupon, response.getBody());
//         verify(couponService).createCoupon(coupon);
//     }

//     @Test
//     void testGetAllCoupons_MixedTypes() {
//         List<Coupon> coupons = List.of(createCartWiseCoupon(1L, "CODE1"), createProductWiseCoupon(2L, "CODE2"));
//         when(couponService.getAllCoupons()).thenReturn(coupons);

//         ResponseEntity<List<Coupon>> response = couponController.getAllCoupons();

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupons, response.getBody());
//         verify(couponService).getAllCoupons();
//     }

//     @Test
//     void testGetCouponById_ProductWise() {
//         Coupon coupon = createProductWiseCoupon(2L, "CODE2");
//         when(couponService.getCouponById(2L)).thenReturn(Optional.of(coupon));

//         ResponseEntity<Coupon> response = couponController.getCouponById(2L);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupon, response.getBody());
//         verify(couponService).getCouponById(2L);
//     }

//     @Test
//     void testUpdateCoupon_ProductWise_Success() {
//         Coupon couponDetails = createProductWiseCoupon(2L, "CODE2");
//         Coupon updatedCoupon = createProductWiseCoupon(2L, "CODE2");
//         when(couponService.updateCoupon(eq(2L), any())).thenReturn(updatedCoupon);

//         ResponseEntity<Coupon> response = couponController.updateCoupon(2L, couponDetails);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(updatedCoupon, response.getBody());
//         verify(couponService).updateCoupon(2L, couponDetails);
//     }

//     @Test
//     void testDeleteCoupon_ProductWise_Success() {
//         doNothing().when(couponService).deleteCoupon(2L);

//         ResponseEntity<Void> response = couponController.deleteCoupon(2L);

//         assertEquals(200, response.getStatusCode().value());
//         assertNull(response.getBody());
//         verify(couponService).deleteCoupon(2L);
//     }

//     @Test
//     void testGetApplicableCoupons_EmptyCart() {
//         Cart cart = createCart("2", 0, new ArrayList<>());
//         when(couponService.getApplicableCoupons(cart)).thenReturn(Collections.emptyList());

//         ResponseEntity<List<ApplicableCouponResponse>> response = couponController.getApplicableCoupons(cart);

//         assertEquals(200, response.getStatusCode().value());
//         assertTrue(response.getBody().isEmpty());
//         verify(couponService).getApplicableCoupons(cart);
//     }

//     @Test
//     void testApplyCoupon_ProductWise_Success() {
//         ApplyCouponRequest request = new ApplyCouponRequest();
//         request.setCartId("2");
//         List<CartItem> items = List.of(createCartItem("P1", 100, 2));
//         request.setItems(items);

//         Cart updatedCart = createCart("2", 170, items);
//         when(couponService.calculateTotalAmount(items)).thenReturn(200.0);
//         when(couponService.applyCoupon(2L, any())).thenReturn(updatedCart);

//         ResponseEntity<?> response = couponController.applyCoupon(2L, request);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(updatedCart, response.getBody());
//         verify(couponService).applyCoupon(eq(2L), any());
//     }

//     @Test
//     void testApplyCoupon_ExceptionMessage() {
//         ApplyCouponRequest request = new ApplyCouponRequest();
//         request.setCartId("3");
//         List<CartItem> items = List.of(createCartItem("P2", 50, 1));
//         request.setItems(items);

//         when(couponService.calculateTotalAmount(items)).thenReturn(50.0);
//         when(couponService.applyCoupon(3L, any())).thenThrow(new RuntimeException("Unexpected error"));

//         ResponseEntity<?> response = couponController.applyCoupon(3L, request);

//         assertEquals(400, response.getStatusCode().value());
//         assertEquals("Unexpected error", response.getBody());
//         verify(couponService).applyCoupon(eq(3L), any());
//     }

//     // private CartItem createCartItem(String productId, double price, int quantity) {
//     //     CartItem item = new CartItem();
//     //     item.setProductId(productId);
//     //     item.setPrice(price);
//     //     item.setQuantity(quantity);
//     //     return item;
//     // }

//     @Test
//     void testCreateCoupon() {
//         Coupon coupon = createCoupon(1L, "CODE1");
//         when(couponService.createCoupon(any())).thenReturn(coupon);

//         ResponseEntity<Coupon> response = couponController.createCoupon(coupon);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupon, response.getBody());
//         verify(couponService).createCoupon(coupon);
//     }

//     @Test
//     void testGetAllCoupons() {
//         List<Coupon> coupons = List.of(createCoupon(1L, "CODE1"), createCoupon(2L, "CODE2"));
//         when(couponService.getAllCoupons()).thenReturn(coupons);

//         ResponseEntity<List<Coupon>> response = couponController.getAllCoupons();

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupons, response.getBody());
//         verify(couponService).getAllCoupons();
//     }

//     @Test
//     void testGetCouponById_Found() {
//         Coupon coupon = createCoupon(1L, "CODE1");
//         when(couponService.getCouponById(1L)).thenReturn(Optional.of(coupon));

//         ResponseEntity<Coupon> response = couponController.getCouponById(1L);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(coupon, response.getBody());
//         verify(couponService).getCouponById(1L);
//     }

//     @Test
//     void testGetCouponById_NotFound() {
//         when(couponService.getCouponById(2L)).thenReturn(Optional.empty());

//         ResponseEntity<Coupon> response = couponController.getCouponById(2L);

//         assertEquals(404, response.getStatusCode().value());
//         assertNull(response.getBody());
//         verify(couponService).getCouponById(2L);
//     }

//     @Test
//     void testUpdateCoupon_Success() {
//         Coupon couponDetails = createCoupon(1L, "CODE1");
//         Coupon updatedCoupon = createCoupon(1L, "CODE1");
//         when(couponService.updateCoupon(eq(1L), any())).thenReturn(updatedCoupon);

//         ResponseEntity<Coupon> response = couponController.updateCoupon(1L, couponDetails);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(updatedCoupon, response.getBody());
//         verify(couponService).updateCoupon(1L, couponDetails);
//     }

//     @Test
//     void testUpdateCoupon_NotFound() {
//         Coupon couponDetails = createCoupon(2L, "CODE2");
//         when(couponService.updateCoupon(eq(2L), any())).thenThrow(new RuntimeException("Not found"));

//         ResponseEntity<Coupon> response = couponController.updateCoupon(2L, couponDetails);

//         assertEquals(404, response.getStatusCode().value());
//         assertNull(response.getBody());
//         verify(couponService).updateCoupon(2L, couponDetails);
//     }

//     @Test
//     void testDeleteCoupon_Success() {
//         doNothing().when(couponService).deleteCoupon(1L);

//         ResponseEntity<Void> response = couponController.deleteCoupon(1L);

//         assertEquals(200, response.getStatusCode().value());
//         assertNull(response.getBody());
//         verify(couponService).deleteCoupon(1L);
//     }

//     @Test
//     void testDeleteCoupon_NotFound() {
//         doThrow(new RuntimeException("Not found")).when(couponService).deleteCoupon(2L);

//         ResponseEntity<Void> response = couponController.deleteCoupon(2L);

//         assertEquals(404, response.getStatusCode().value());
//         assertNull(response.getBody());
//         verify(couponService).deleteCoupon(2L);
//     }

//     @Test
//     void testGetApplicableCoupons() {
//         Cart cart = createCart("1", 200, List.of(createCartItem("P1", 100, 2)));
//         List<ApplicableCouponResponse> applicableCoupons = List.of(new ApplicableCouponResponse());
//         when(couponService.getApplicableCoupons(cart)).thenReturn(applicableCoupons);

//         ResponseEntity<List<ApplicableCouponResponse>> response = couponController.getApplicableCoupons(cart);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(applicableCoupons, response.getBody());
//         verify(couponService).getApplicableCoupons(cart);
//     }

//     @Test
//     void testApplyCoupon_Success() {
//         ApplyCouponRequest request = new ApplyCouponRequest();
//         request.setCartId("1");
//         List<CartItem> items = List.of(createCartItem("P1", 100, 2));
//         request.setItems(items);

//         Cart updatedCart = createCart("1", 180, items);
//         when(couponService.calculateTotalAmount(items)).thenReturn(200.0);
//         when(couponService.applyCoupon(1L, any())).thenReturn(updatedCart);

//         ResponseEntity<?> response = couponController.applyCoupon(1L, request);

//         assertEquals(200, response.getStatusCode().value());
//         assertEquals(updatedCart, response.getBody());
//         verify(couponService).applyCoupon(eq(1L), any());
//     }

//     @Test
//     void testApplyCoupon_NotFound() {
//         ApplyCouponRequest request = new ApplyCouponRequest();
//         request.setCartId("1");
//         List<CartItem> items = List.of(createCartItem("P1", 100, 2));
//         request.setItems(items);

//         when(couponService.calculateTotalAmount(items)).thenReturn(200.0);
//         when(couponService.applyCoupon(1L, any())).thenThrow(new CouponNotFoundException("Coupon not found"));

//         ResponseEntity<?> response = couponController.applyCoupon(1L, request);

//         assertEquals(404, response.getStatusCode().value());
//         assertEquals("Coupon not found", response.getBody());
//         verify(couponService).applyCoupon(eq(1L), any());
//     }

//     @Test
//     void testApplyCoupon_OtherException() {
//         ApplyCouponRequest request = new ApplyCouponRequest();
//         request.setCartId("1");
//         List<CartItem> items = List.of(createCartItem("P1", 100, 2));
//         request.setItems(items);

//         when(couponService.calculateTotalAmount(items)).thenReturn(200.0);
//         when(couponService.applyCoupon(1L, any())).thenThrow(new IllegalArgumentException("Invalid coupon"));

//         ResponseEntity<?> response = couponController.applyCoupon(1L, request);

//         assertEquals(400, response.getStatusCode().value());
//         assertEquals("Invalid coupon", response.getBody());
//         verify(couponService).applyCoupon(eq(1L), any());
//     }
// }