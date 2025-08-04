package com.loopers.application.order;

import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.*;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
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
    private PointRepository pointRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private CouponJpaRepository couponRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private UserId userId;
    private OrderCommand orderCommand;
    private List<OrderItemCommand> orderItemCommands;
    private List<ProductModel> products;
    private List<OrderItemModel> orderItems;
    private PointModel availablePoint;
    private OrderModel order;
    private CouponModel coupon;
    private UserCouponModel userCoupon;

    @BeforeEach
    void setUp() {
        userId = UserId.of("seyoung");
        coupon = CouponModel.builder()
            .name("TEST_COUPON")
            .type(CouponType.FIXED_AMOUNT)
            .discountValue(5000)
            .minimumOrderAmount(30000)
            .maximumDiscountAmount(5000)
            .issuedAt(LocalDate.now())
            .validUntil(LocalDate.now().plusDays(30))
            .build();

        userCoupon = UserCouponModel.create(userId, coupon.getCouponCode());
        couponRepository.save(coupon);
        userCouponRepository.save(userCoupon);

        UserModel user = UserModel.of(userId, Email.of("seyoung@loopers.com"), Gender.FEMALE, BirthDate.of("2000-01-01"));
        userRepository.save(user);

        // 테스트 데이터 저장
        ProductModel product1 = ProductModel.builder()
            .brandId(1L)
            .categoryId(1L)
            .name("Apple iPhone 14")
            .price(1000000)
            .stock(10)
            .description("Apple iPhone 14")
            .likesCount(0)
            .build();
        productRepository.save(product1);
            
        ProductModel product2 = ProductModel.builder()
            .brandId(1L)
            .categoryId(1L)
            .name("Samsung Galaxy S23")
            .price(1200000)
            .stock(5)
            .description("Samsung Galaxy S23")
            .likesCount(0)
            .build();
        productRepository.save(product2);
            
        products = List.of(product1, product2);

        orderItemCommands = List.of(
            new OrderItemCommand(null, products.get(0).getId(), 2, "Apple iPhone 14", 1000000),
            new OrderItemCommand(null, products.get(1).getId(), 1, "Samsung Galaxy S23", 1200000)
        );
        
        orderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, coupon.getCouponCode(), 50000, orderItemCommands);

        orderItems = List.of(
            OrderItemModel.builder().productId(products.get(0).getId()).quantity(2).priceAtOrder(1000000).build(),
            OrderItemModel.builder().productId(products.get(1).getId()).quantity(1).priceAtOrder(1200000).build()
        );

        availablePoint = pointRepository.save(PointModel.of(userId, 100000));
        order = OrderModel.create(userId, 3150000); // (1000000 * 2 + 1200000 * 1) - 50000
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
        assertThat(result.totalPrice()).isEqualTo(3145000); // (1000000 * 2 + 1200000 * 1) - 50000 - 5000(쿠폰할인)
        assertThat(result.quantity()).isEqualTo(3); // 2 + 1

        // DB 저장 확인
        assertThat(orderRepository.findById(result.orderId())).isPresent();
        List<OrderItemModel> savedOrderItems = orderItemRepository.findByOrderId(result.orderId());
        assertThat(savedOrderItems).hasSize(2);
        
        // 포인트 차감 확인
        Optional<PointModel> updatedPoint = pointRepository.findByUserId(userId);
        assertThat(updatedPoint).isPresent();
        assertThat(updatedPoint.get().getAmount()).isEqualTo(50000); // 100000 - 50000
        
        // 상품 재고 차감 확인
        Optional<ProductModel> updatedProduct1 = productRepository.findById(products.get(0).getId());
        Optional<ProductModel> updatedProduct2 = productRepository.findById(products.get(1).getId());
        assertThat(updatedProduct1).isPresent();
        assertThat(updatedProduct2).isPresent();
        assertThat(updatedProduct1.get().getStock()).isEqualTo(8); // 10 - 2
        assertThat(updatedProduct2.get().getStock()).isEqualTo(4); // 5 - 1
        
    }

    @Test
    @DisplayName("포인트가 없는 사용자의 주문 생성")
    void createOrderWithoutPoints() {
        // given
        OrderCommand orderCommandWithoutPoints = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, coupon.getCouponCode(),0, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(orderCommandWithoutPoints);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice()).isEqualTo(3195000); // (1000000 * 2 + 1200000 * 1) - 5000(쿠폰할인)
        
        // 포인트가 차감되지 않았는지 확인
        Optional<PointModel> updatedPoint = pointRepository.findByUserId(userId);
        assertThat(updatedPoint).isPresent();
        assertThat(updatedPoint.get().getAmount()).isEqualTo(100000); // 변화 없음
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 주문 생성 시도")
    void createOrderWithNonExistentUser() {
        // given
        UserId nonExistentUserId = UserId.of("seyoung12");
        OrderCommand invalidOrderCommand = new OrderCommand(nonExistentUserId, PaymentMethod.CREDIT_CARD, coupon.getCouponCode(),0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(invalidOrderCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("재고 부족으로 인한 주문 생성 실패")
    void createOrderWithInsufficientStock() {
        // given
        // 재고가 0인 상품 생성
        ProductModel outOfStockProduct = ProductModel.builder()
            .brandId(1L)
            .categoryId(1L)
            .name("테스트상품")
            .price(50000)
            .stock(0)
            .description("테스트상품")
            .likesCount(0)
            .build();
        productRepository.save(outOfStockProduct);

        List<OrderItemCommand> invalidOrderItemCommands = List.of(
            new OrderItemCommand(null, outOfStockProduct.getId(), 1, "테스트상품", 50000)
        );
        
        OrderCommand invalidOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, coupon.getCouponCode(),0, invalidOrderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(invalidOrderCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
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
        couponRepository.save(percentageCoupon);

        UserCouponModel percentageUserCoupon = UserCouponModel.create(userId, percentageCoupon.getCouponCode());
        userCouponRepository.save(percentageUserCoupon);

        OrderCommand percentageOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, percentageCoupon.getCouponCode(), 0, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(percentageOrderCommand);

        // then
        assertThat(result).isNotNull();
        // 주문 금액: (1000000 * 2 + 1200000 * 1) = 3200000
        // 10% 할인: 3200000 * 0.1 = 320000, 최대 할인 금액 100000 적용
        // 최종 금액: 3200000 - 100000 = 3100000
        assertThat(result.totalPrice()).isEqualTo(3100000);
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
        couponRepository.save(expiredCoupon);

        UserCouponModel expiredUserCoupon = UserCouponModel.create(userId, expiredCoupon.getCouponCode());
        userCouponRepository.save(expiredUserCoupon);

        OrderCommand expiredOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, expiredCoupon.getCouponCode(), 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(expiredOrderCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰 사용 시도")
    void createOrderWithUsedCoupon() {
        // given
        userCoupon.useCoupon(LocalDate.now());
        userCouponRepository.save(userCoupon);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(orderCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
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
        couponRepository.save(highMinimumCoupon);

        UserCouponModel highMinimumUserCoupon = UserCouponModel.create(userId, highMinimumCoupon.getCouponCode());
        userCouponRepository.save(highMinimumUserCoupon);

        OrderCommand highMinimumOrderCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, highMinimumCoupon.getCouponCode(), 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(highMinimumOrderCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 사용 시도")
    void createOrderWithNonExistentCoupon() {
        // given
        OrderCommand nonExistentCouponCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, "NON_EXISTENT_COUPON", 0, orderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(nonExistentCouponCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("쿠폰 없이 주문 생성")
    void createOrderWithoutCoupon() {
        // given
        OrderCommand noCouponCommand = new OrderCommand(userId, PaymentMethod.CREDIT_CARD, null, 0, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(noCouponCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice()).isEqualTo(3200000); // (1000000 * 2 + 1200000 * 1) - 할인 없음
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
        assertThat(usedUserCoupon.get().isUsed()).isTrue();
        assertThat(usedUserCoupon.get().getUsedAt()).isNotNull();
    }
}
