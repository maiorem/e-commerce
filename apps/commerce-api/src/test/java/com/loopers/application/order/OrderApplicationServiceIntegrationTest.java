package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.coupon.*;
import com.loopers.domain.order.*;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.*;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
@Transactional
class OrderApplicationServiceIntegrationTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponJpaRepository couponRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private CouponProcessor couponProcessor;

    private UserId userId;
    private OrderCommand orderCommand;
    private List<OrderItemCommand> orderItemCommands;
    private List<ProductModel> products;
    private CouponModel coupon;
    private UserCouponModel userCoupon;
    private BrandModel brand;
    private CategoryModel category;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();

        mock(couponProcessor);
        
        userId = UserId.of("seyoung");
        
        // User 먼저 생성
        UserModel user = UserModel.of(userId, Email.of("seyoung@loopers.com"), Gender.FEMALE, BirthDate.of("2000-01-01"));
        userRepository.save(user);

        // Brand와 Category 먼저 생성
        brand = BrandModel.of("Apple", "Apple Inc.");
        brand = brandJpaRepository.saveAndFlush(brand);
        
        category = CategoryModel.of("전자제품", "전자제품 카테고리");
        category = categoryJpaRepository.saveAndFlush(category);
        
        // Coupon 생성
        coupon = CouponModel.builder()
            .name("TEST_COUPON")
            .type(CouponType.FIXED_AMOUNT)
            .discountValue(5000)
            .minimumOrderAmount(30000)
            .maximumDiscountAmount(5000)
            .issuedAt(LocalDate.now())
            .validUntil(LocalDate.now().plusDays(30))
            .build();
        coupon = couponRepository.saveAndFlush(coupon);
        
        // UserCoupon 생성
        userCoupon = UserCouponModel.create(userId, coupon.getCouponCode());
        userCouponRepository.save(userCoupon);
        
        // Product 생성
        ProductModel product1 = ProductModel.builder()
            .brandId(brand.getId())
            .categoryId(category.getId())
            .name("iPhone 15")
            .price(1000000)
            .stock(10)
            .description("iPhone 15")
            .likesCount(0)
            .build();
        product1 = productJpaRepository.saveAndFlush(product1);
        
        ProductModel product2 = ProductModel.builder()
            .brandId(brand.getId())
            .categoryId(category.getId())
            .name("MacBook Pro")
            .price(1200000)
            .stock(5)
            .description("MacBook Pro")
            .likesCount(0)
            .build();
        product2 = productJpaRepository.saveAndFlush(product2);
        
        products = List.of(product1, product2);
        
        // OrderItemCommand 생성
        orderItemCommands = List.of(
            new OrderItemCommand(null, product1.getId(), 2, "iPhone 15", 1000000),
            new OrderItemCommand(null, product2.getId(), 1, "MacBook Pro", 1200000)
        );
        
        // OrderCommand 생성
        orderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", coupon.getCouponCode(), 0, orderItemCommands);

        // Mock CouponProcessor 설정
        when(couponProcessor.applyCouponDiscount(userId, 3200000, coupon.getCouponCode())).thenReturn(3195000);
        when(couponProcessor.applyCouponDiscount(userId, 3200000, null)).thenReturn(3200000);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("정상적인 주문 생성 통합 테스트")
    void createOrderIntegrationTest() {
        // when
        OrderInfo result = orderApplicationService.createOrder(orderCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice().getAmount()).isEqualTo(3195000); // (1000000 * 2 + 1200000 * 1) - 5000(쿠폰할인)
        assertThat(result.quantity()).isEqualTo(3); // 2 + 1

        // DB 저장 확인
        assertThat(orderRepository.findById(result.orderId())).isPresent();
        List<OrderItemModel> savedOrderItems = orderItemRepository.findByOrderId(result.orderId());
        assertThat(savedOrderItems).hasSize(2);
        
        // 상품 재고 차감 확인
        Optional<ProductModel> updatedProduct1 = productRepository.findById(products.get(0).getId());
        Optional<ProductModel> updatedProduct2 = productRepository.findById(products.get(1).getId());
        assertThat(updatedProduct1).isPresent();
        assertThat(updatedProduct2).isPresent();
        assertThat(updatedProduct1.get().getStock()).isEqualTo(8); // 10 - 2
        assertThat(updatedProduct2.get().getStock()).isEqualTo(4); // 5 - 1
    }

    @Test
    @DisplayName("카드 결제로 주문 생성 및 결제 요청 이벤트 발행 테스트")
    void createOrderAndRequestPaymentWithCard() {

        // when
        OrderInfo result = orderApplicationService.createOrder(orderCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice().getAmount()).isEqualTo(3195000); // 쿠폰 할인 적용된 금액
        
        // 주문이 정상적으로 생성되었는지 확인
        Optional<OrderModel> savedOrder = orderRepository.findById(result.orderId());
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("포인트 결제로 주문 생성 및 결제 요청 이벤트 발행 테스트")
    void createOrderAndRequestPaymentWithPoint() {
        // given
        OrderCommand pointOrderCommand = new OrderCommand(userId, PaymentMethod.POINT, null, null, coupon.getCouponCode(), 100000, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(pointOrderCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice().getAmount()).isEqualTo(3195000); // 쿠폰 할인 적용된 금액
        
        // 주문이 정상적으로 생성되었는지 확인
        Optional<OrderModel> savedOrder = orderRepository.findById(result.orderId());
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    @DisplayName("포인트가 없는 사용자의 주문 생성")
    void createOrderWithoutPoints() {
        // given
        OrderCommand orderCommandWithoutPoints = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", coupon.getCouponCode(), 0, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(orderCommandWithoutPoints);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice().getAmount()).isEqualTo(3195000); // (1000000 * 2 + 1200000 * 1) - 5000(쿠폰할인)
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 주문 생성 시도")
    void createOrderWithNonExistentUser() {
        // given
        UserId nonExistentUserId = UserId.of("seyoung12");
        OrderCommand invalidOrderCommand = new OrderCommand(nonExistentUserId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", coupon.getCouponCode(), 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(invalidOrderCommand))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("재고 부족으로 인한 주문 생성 실패")
    void createOrderWithInsufficientStock() {
        // given
        // 재고가 0인 상품 생성
        ProductModel outOfStockProduct = ProductModel.builder()
            .brandId(brand.getId())
            .categoryId(category.getId())
            .name("테스트상품")
            .price(50000)
            .stock(0)
            .description("테스트상품")
            .likesCount(0)
            .build();
        outOfStockProduct = productJpaRepository.saveAndFlush(outOfStockProduct);

        List<OrderItemCommand> invalidOrderItemCommands = List.of(
            new OrderItemCommand(null, outOfStockProduct.getId(), 1, "테스트상품", 50000)
        );
        
        OrderCommand invalidOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", coupon.getCouponCode(), 0, invalidOrderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(invalidOrderCommand))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정률 할인 쿠폰 사용 테스트")
    void createOrderWithPercentageCoupon() {
        // given
        CouponModel percentageCoupon = CouponModel.builder()
            .name("PERCENTAGE_COUPON")
            .type(CouponType.PERCENTAGE)
            .discountValue(10) // 10% 할인
            .minimumOrderAmount(30000)
            .maximumDiscountAmount(100000)
            .issuedAt(LocalDate.now())
            .validUntil(LocalDate.now().plusDays(30))
            .build();
        percentageCoupon = couponRepository.saveAndFlush(percentageCoupon);

        UserCouponModel percentageUserCoupon = UserCouponModel.create(userId, percentageCoupon.getCouponCode());
        userCouponRepository.save(percentageUserCoupon);

        OrderCommand percentageOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", percentageCoupon.getCouponCode(), 0, orderItemCommands);

        // Mock CouponProcessor 설정 for percentage coupon
        when(couponProcessor.applyCouponDiscount(userId, 3200000, percentageCoupon.getCouponCode())).thenReturn(3100000);

        // when
        OrderInfo result = orderApplicationService.createOrder(percentageOrderCommand);

        // then
        assertThat(result).isNotNull();
        // 주문 금액: (1000000 * 2 + 1200000 * 1) = 3200000
        // 10% 할인: 3200000 * 0.1 = 320000, 최대 할인 금액 100000 적용
        // 최종 금액: 3200000 - 100000 = 3100000
        assertThat(result.totalPrice().getAmount()).isEqualTo(3100000);
    }

    @Test
    @DisplayName("만료된 쿠폰 사용 시도")
    void createOrderWithExpiredCoupon() {
        // given
        CouponModel expiredCoupon = CouponModel.builder()
            .name("EXPIRED_COUPON")
            .type(CouponType.FIXED_AMOUNT)
            .discountValue(5000)
            .minimumOrderAmount(30000)
            .maximumDiscountAmount(5000)
            .issuedAt(LocalDate.now().minusDays(60))
            .validUntil(LocalDate.now().minusDays(1)) // 어제 만료
            .build();
        expiredCoupon = couponRepository.saveAndFlush(expiredCoupon);

        UserCouponModel expiredUserCoupon = UserCouponModel.create(userId, expiredCoupon.getCouponCode());
        userCouponRepository.save(expiredUserCoupon);

        OrderCommand expiredOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", expiredCoupon.getCouponCode(), 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(expiredOrderCommand))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰 사용 시도")
    void createOrderWithUsedCoupon() {
        // given
        CouponModel usedCoupon = CouponModel.builder()
            .name("USED_COUPON")
            .type(CouponType.FIXED_AMOUNT)
            .discountValue(5000)
            .minimumOrderAmount(30000)
            .maximumDiscountAmount(5000)
            .issuedAt(LocalDate.now().minusDays(10))
            .validUntil(LocalDate.now().plusDays(20))
            .build();
        usedCoupon = couponRepository.saveAndFlush(usedCoupon);
        
        UserCouponModel usedUserCoupon = UserCouponModel.create(userId, usedCoupon.getCouponCode());
        usedUserCoupon.useCoupon(LocalDate.now());
        userCouponRepository.save(usedUserCoupon);
        
        OrderCommand orderCommandWithUsedCoupon = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", usedCoupon.getCouponCode(), 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(orderCommandWithUsedCoupon))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("최소 주문 금액 미달로 쿠폰 사용 실패")
    void createOrderWithCouponBelowMinimumAmount() {
        // given
        CouponModel highMinimumCoupon = CouponModel.builder()
            .name("HIGH_MINIMUM_COUPON")
            .type(CouponType.FIXED_AMOUNT)
            .discountValue(5000)
            .minimumOrderAmount(5000000)
            .maximumDiscountAmount(5000)
            .issuedAt(LocalDate.now())
            .validUntil(LocalDate.now().plusDays(30))
            .build();
        highMinimumCoupon = couponRepository.saveAndFlush(highMinimumCoupon);

        UserCouponModel highMinimumUserCoupon = UserCouponModel.create(userId, highMinimumCoupon.getCouponCode());
        userCouponRepository.save(highMinimumUserCoupon);

        OrderCommand highMinimumOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", highMinimumCoupon.getCouponCode(), 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(highMinimumOrderCommand))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 사용 시도")
    void createOrderWithNonExistentCoupon() {
        // given
        OrderCommand nonExistentCouponCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", "NON_EXISTENT_COUPON", 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(nonExistentCouponCommand))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("쿠폰 없이 주문 생성")
    void createOrderWithoutCoupon() {
        // given
        OrderCommand noCouponCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, CardType.SAMSUNG, "1234567890123456", null, 0, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(noCouponCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice().getAmount()).isEqualTo(3200000); // (1000000 * 2 + 1200000 * 1) - 할인 없음
    }

    @Test
    @DisplayName("쿠폰 사용 후 상태 변경 확인")
    void verifyCouponUsageStatusAfterOrder() {
        // when
        OrderInfo result = orderApplicationService.createOrder(orderCommand);

        // then
        assertThat(result).isNotNull();
        
        Optional<UserCouponModel> usedUserCoupon = userCouponRepository.findByUserIdAndCouponCode(userId, coupon.getCouponCode());
        assertThat(usedUserCoupon).isPresent();
        assertThat(usedUserCoupon.get().getStatus()).isEqualTo(UserCoupontStatus.RESERVED); // 주문 생성 시 RESERVED 상태
        assertThat(usedUserCoupon.get().getUsedAt()).isNull(); // 아직 사용되지 않음
    }
}
