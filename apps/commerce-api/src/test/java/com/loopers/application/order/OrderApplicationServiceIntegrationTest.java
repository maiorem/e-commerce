package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.*;
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
    private DatabaseCleanUp databaseCleanUp;

    private UserId userId;
    private OrderCommand orderCommand;
    private List<OrderItemCommand> orderItemCommands;
    private List<ProductModel> products;
    private List<OrderItemModel> orderItems;
    private PointModel availablePoint;
    private OrderModel order;

    @BeforeEach
    void setUp() {
        userId = UserId.of("seyoung");

        UserModel user = UserModel.of(userId, Email.of("seyoung@loopers.com"), Gender.FEMALE, BirthDate.of("2000-01-01"));
        userRepository.save(user);

        // 테스트 데이터 저장
        ProductModel product1 = ProductModel.builder()
            .name("Apple iPhone 14")
            .price(1000000)
            .stock(10)
            .description("Apple iPhone 14")
            .build();
        productRepository.save(product1);
            
        ProductModel product2 = ProductModel.builder()
            .name("Samsung Galaxy S23")
            .price(1200000)
            .stock(5)
            .description("Samsung Galaxy S23")
            .build();
        productRepository.save(product2);
            
        products = List.of(product1, product2);

        orderItemCommands = List.of(
            new OrderItemCommand(1L, products.get(0).getId(), 2, "Apple iPhone 14", 1000000),
            new OrderItemCommand(1L, products.get(1).getId(), 1, "Samsung Galaxy S23", 1200000)
        );
        
        orderCommand = new OrderCommand(userId, 50000, orderItemCommands);

        orderItems = List.of(
            new OrderItemModel(1L, products.get(0).getId(), 2, 1000000),
            new OrderItemModel(1L, products.get(1).getId(), 1, 1200000)
        );

        availablePoint = pointRepository.save(PointModel.of(userId, 100000));
        order = OrderModel.of(userId, 3150000); // (1000000 * 2 + 1200000 * 1) - 50000
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
        assertThat(result.totalPrice()).isEqualTo(3150000); // (1000000 * 2 + 1200000 * 1) - 50000
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
        OrderCommand orderCommandWithoutPoints = new OrderCommand(userId, 0, orderItemCommands);

        // when
        OrderInfo result = orderApplicationService.createOrder(orderCommandWithoutPoints);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalPrice()).isEqualTo(3200000); // 포인트 차감 없음
        
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
        OrderCommand invalidOrderCommand = new OrderCommand(nonExistentUserId, 0, orderItemCommands);

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
            .name("테스트상품")
            .price(50000)
            .stock(0)
            .description("테스트상품")
            .build();
        productRepository.save(outOfStockProduct);

        List<OrderItemCommand> invalidOrderItemCommands = List.of(
            new OrderItemCommand(1L, outOfStockProduct.getId(), 1, "테스트상품", 50000)
        );
        
        OrderCommand invalidOrderCommand = new OrderCommand(userId, 0, invalidOrderItemCommands);

        // when & then
        assertThatThrownBy(() -> orderApplicationService.createOrder(invalidOrderCommand))
            .isInstanceOf(CoreException.class)
            .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }


}
