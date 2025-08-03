package com.loopers.domain.order;

import com.loopers.domain.product.ProductModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCreationDomainServiceTest {

    private OrderCreationDomainService orderCreationDomainService;

    @BeforeEach
    void setUp() {
        orderCreationDomainService = new OrderCreationDomainService();
    }

    // 리플렉션으로 product 에 ID 강제 설정
    private ProductModel createProductWithId(Long id, String name, int price, int stock) {
        ProductModel product = ProductModel.builder()
            .name(name)
            .price(price)
            .stock(stock)
            .description(name)
            .build();
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Nested
    @DisplayName("주문 아이템 검증")
    class Validate_Order_Items {

        @Test
        @DisplayName("유효한 주문 아이템들이면 검증을 통과한다")
        void validateOrderItemsSuccess() {
            // given
            ProductModel product1 = createProductWithId(1L, "상품1", 10000, 10);
            ProductModel product2 = createProductWithId(2L, "상품2", 20000, 5);

            List<ProductModel> products = List.of(product1, product2);

            OrderItemModel orderItem1 = new OrderItemModel(1L, 1L, 2, 10000);
            OrderItemModel orderItem2 = new OrderItemModel(1L, 2L, 1, 20000);

            List<OrderItemModel> orderItems = List.of(orderItem1, orderItem2);

            // when & then
            orderCreationDomainService.validateOrderItems(orderItems, products);
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID가 포함되면 예외가 발생한다")
        void validateOrderItemsWithNonExistentProduct() {
            // given
            ProductModel product = createProductWithId(1L, "상품1", 10000, 10);

            List<ProductModel> products = List.of(product);

            OrderItemModel orderItem = new OrderItemModel(1L, 999L, 1, 10000);
            List<OrderItemModel> orderItems = List.of(orderItem);

            // when & then
            assertThatThrownBy(() -> orderCreationDomainService.validateOrderItems(orderItems, products))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
        }

        @Test
        @DisplayName("수량이 0 이하면 예외가 발생한다")
        void validateOrderItemsWithZeroQuantity() {
            // given
            ProductModel product = createProductWithId(1L, "상품1", 10000, 10);

            List<ProductModel> products = List.of(product);

            OrderItemModel orderItem = new OrderItemModel(1L, 1L, 0, 10000);
            List<OrderItemModel> orderItems = List.of(orderItem);

            // when & then
            assertThatThrownBy(() -> orderCreationDomainService.validateOrderItems(orderItems, products))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("재고가 부족하면 예외가 발생한다")
        void validateOrderItemsWithInsufficientStock() {
            // given
            ProductModel product = createProductWithId(1L, "상품1", 10000, 5);

            List<ProductModel> products = List.of(product);

            OrderItemModel orderItem = new OrderItemModel(1L, 1L, 10, 10000);
            List<OrderItemModel> orderItems = List.of(orderItem);

            // when & then
            assertThatThrownBy(() -> orderCreationDomainService.validateOrderItems(orderItems, products))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("주문 가격 계산 시,")
    class Calculate_Order_Price {

        @Test
        @DisplayName("주문 아이템들의 총 가격을 계산한다")
        void calculateOrderPriceSuccess() {
            // given
            OrderItemModel orderItem1 = new OrderItemModel(1L, 1L, 2, 10000);
            OrderItemModel orderItem2 = new OrderItemModel(1L, 2L, 1, 20000);
            List<OrderItemModel> orderItems = List.of(orderItem1, orderItem2);

            // when
            int totalPrice = orderCreationDomainService.calculateOrderPrice(orderItems);

            // then
            assertThat(totalPrice).isEqualTo(40000); // (10000 * 2) + (20000 * 1)
        }

        @Test
        @DisplayName("빈 주문 아이템 리스트면 0을 반환한다")
        void calculateOrderPriceWithEmptyList() {
            // given
            List<OrderItemModel> orderItems = List.of();

            // when
            int totalPrice = orderCreationDomainService.calculateOrderPrice(orderItems);

            // then
            assertThat(totalPrice).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("최종 결제 금액 계산 시,")
    class Calculate_Final_Total_Price {

        @Test
        @DisplayName("주문 가격에서 사용 포인트를 차감한 금액을 계산한다")
        void calculateFinalTotalPriceSuccess() {
            // given
            int orderPrice = 50000;
            int usedPoints = 10000;

            // when
            int finalPrice = orderCreationDomainService.calculateFinalTotalPrice(orderPrice, usedPoints);

            // then
            assertThat(finalPrice).isEqualTo(40000);
        }

        @Test
        @DisplayName("사용 포인트가 0이면 주문 가격과 동일하다")
        void calculateFinalTotalPriceWithZeroPoints() {
            // given
            int orderPrice = 50000;
            int usedPoints = 0;

            // when
            int finalPrice = orderCreationDomainService.calculateFinalTotalPrice(orderPrice, usedPoints);

            // then
            assertThat(finalPrice).isEqualTo(50000);
        }

        @Test
        @DisplayName("사용 포인트가 주문 가격보다 크면 음수를 반환한다")
        void calculateFinalTotalPriceWithExcessivePoints() {
            // given
            int orderPrice = 50000;
            int usedPoints = 60000;

            // when
            int finalPrice = orderCreationDomainService.calculateFinalTotalPrice(orderPrice, usedPoints);

            // then
            assertThat(finalPrice).isEqualTo(-10000);
        }
    }
}
