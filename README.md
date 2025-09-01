# Coupon Service

A Spring Boot application for managing and applying various types of coupons to shopping carts.

## Features

### Implemented Coupon Types

1. **Cart-wise Coupons**
   - Apply discount based on total cart amount
   - Supports percentage and fixed amount discounts
   - Minimum cart amount requirement

2. **Product-wise Coupons**
   - Apply discount to specific products
   - Supports percentage and fixed amount discounts per product
   - Multiple products can be specified

3. **BxGy (Buy X Get Y) Coupons**
   - Buy specified quantity from one set of products
   - Get specified quantity from another set free
   - Repetition limit support
   - Complex scenario handling

### API Endpoints

- `POST /api/coupons` - Create new coupon
- `GET /api/coupons` - Get all coupons
- `GET /api/coupons/{id}` - Get specific coupon
- `PUT /api/coupons/{id}` - Update coupon
- `DELETE /api/coupons/{id}` - Delete coupon
- `POST /api/coupons/applicable-coupons` - Get applicable coupons for cart
- `POST /api/coupons/apply-coupon/{id}` - Apply coupon to cart

## Assumptions

1. **Product Identification**: Products are identified by unique string IDs
2. **Cart Structure**: Cart contains items with product ID, price, and quantity
3. **Coupon Validity**: Coupons have start and end dates for validity
4. **Discount Application**: Only one coupon can be applied at a time (simplified)
5. **Price Calculation**: Prices are in a single currency (e.g., USD)

## Limitations

1. **Single Coupon Application**: The current implementation applies one coupon at a time
2. **Coupon Stacking**: Multiple coupons cannot be combined in a single transaction
3. **BxGy Complexity**: Complex BxGy scenarios like
   ***Scenario 1:*** Cross Products BxGy where Buy Products and Get Products are related dynamically
   ***Description:*** "Buy 2 items from the 'Electronics' category, get 1 item from the 'Accessories' category free—but only if the accessory is logically related (e.g., a laptop bag for a laptop, not a phone case for a TV)."

   ***Difficult to Implement:***
   1.Need to find the relation between buy product and get product dynamically like during purchase
   2.For every item in buy product we need to check in the cart whether any related accessory is present.

   ***Scenario 2:*** Increasing Discounts with More you Buy
   ***Description:*** "Buy 2, get 1 free. Buy 4, get 2 free and a 10% discount on the next 'get' product. Buy 6 from the 'buy' set, get 3 free, the 4th 'get' product at 50% off, and a Rs. 100 coupon for your next purchase."

   ***Difficult to implement:***
   1.We could have multiple discounts being applied to the same 'get' product.
   Example : A single "get" product might be free, while another might have a percentage discount, and the cart might also generate a future credit. Applying these different discount types simultaneously within the same coupon rule violates the principle of simple, composable discount units.
   
   ***Scenario 3:*** BxGy with time based and sequential purchasing of items
   ***Description:*** "Buy 2 glass lenses and get 1 spec frame free. You will get 10% off on sunglasses if purchased within next 10 days."

   ***Difficult to implement:***
   1.The system must remember a user's cart state across multiple, separate transactions and days. This requires persisting a complex state (e.g., user_123 has purchased X, coupon_ABC is now in 'pending Y' state).
   2.The discount engine must now be aware of time, checking purchase history timestamps to validate the sequence. This moves discount calculation from a simple cart analysis to a complex historical data query.

4. **Performance**: Large cart sizes might impact performance due to complex calculations
5. **Currency**: No multi-currency support

## Edge Cases Handled

1. **Expired Coupons**: Coupons outside their validity period are ignored
2. **Minimum Cart Amount**: Cart-wise coupons only apply if threshold is met
3. **Product Availability**: Product-wise coupons only apply if products are in cart
4. **Repetition Limits**: BxGy coupons respect repetition limits
5. **Negative Prices**: Discounts cannot make prices negative
6. **Duplicate Coupon Codes**: Prevent creation of coupons with duplicate codes

## Unimplemented Features

1. **Coupon Stacking**: Ability to apply multiple coupons simultaneously
2. **Category-based Coupons**: Coupons based on product categories
3. **User-specific Coupons**: Coupons tied to specific users
4. **Usage Limits**: Limits on how many times a coupon can be used
5. **Bulk Operations**: Batch coupon creation/application
6. **Advanced BxGy**: More complex BxGy scenarios with mixed products

## Setup and Running

1. **Prerequisites**: Java 17+, Maven
2. **Build**: `mvn clean install`
3. **Run**: `mvn spring-boot:run`
4. **Access**: http://localhost:8080
5. **H2 Console**: http://localhost:8080/h2-console

## Testing

Run unit tests: `mvn test`

## Future Improvements

1. Add Redis caching for frequently accessed coupons
2. Implement coupon usage tracking
3. Add support for coupon combinations and stacking rules
4. Implement bulk coupon operations
5. Add more comprehensive error handling and logging
6. Implement rate limiting for API endpoints
7. Add support for coupon templates and mass generation