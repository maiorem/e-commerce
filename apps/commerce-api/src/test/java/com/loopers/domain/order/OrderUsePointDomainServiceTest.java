package com.loopers.domain.order;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderUsePointDomainServiceTest {

    private OrderUsePointDomainService orderUsePointDomainService;

    @BeforeEach
    void setUp() {
        orderUsePointDomainService = new OrderUsePointDomainService();
    }

    @Nested
    @DisplayName("포인트 사용 계산 시,")
    class Calculate_Use_Point {

        @Test
        @DisplayName("사용 가능한 포인트가 충분하면 요청한 포인트를 반환한다")
        void calculateUsePointWithSufficientPoints() {
            // given
            PointModel availablePoint = PointModel.of(UserId.of("seyoung"), 50000);
            int orderPrice = 100000;
            int requestUsePoint = 30000;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(30000);
        }

        @Test
        @DisplayName("사용 가능한 포인트가 부족하면 보유 포인트만큼 반환한다")
        void calculateUsePointWithInsufficientPoints() {
            // given
            PointModel availablePoint = PointModel.of(UserId.of("seyoung"), 20000);
            int orderPrice = 100000;
            int requestUsePoint = 50000;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(20000);
        }

        @Test
        @DisplayName("주문 가격보다 많은 포인트를 요청하면 주문 가격만큼 반환한다")
        void calculateUsePointExceedingOrderPrice() {
            // given
            PointModel availablePoint = PointModel.of(UserId.of("seyoung"), 50000);
            int orderPrice = 30000;
            int requestUsePoint = 50000;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(30000);
        }

        @Test
        @DisplayName("포인트가 null이면 0을 반환한다")
        void calculateUsePointWithNullPoint() {
            // given
            PointModel availablePoint = null;
            int orderPrice = 100000;
            int requestUsePoint = 30000;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(0);
        }

        @Test
        @DisplayName("포인트 잔액이 0 이하면 0을 반환한다")
        void calculateUsePointWithZeroBalance() {
            // given
            PointModel availablePoint = PointModel.of(UserId.of("seyoung"), 0);
            int orderPrice = 100000;
            int requestUsePoint = 30000;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(0);
        }

        @Test
        @DisplayName("요청 포인트가 0 이하면 0을 반환한다")
        void calculateUsePointWithZeroRequest() {
            // given
            PointModel availablePoint = PointModel.of(UserId.of("seyoung"), 50000);
            int orderPrice = 100000;
            int requestUsePoint = 0;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(0);
        }

        @Test
        @DisplayName("요청 포인트가 음수면 0을 반환한다")
        void calculateUsePointWithNegativeRequest() {
            // given
            PointModel availablePoint = PointModel.of(UserId.of("seyoung"), 50000);
            int orderPrice = 100000;
            int requestUsePoint = -1000;

            // when
            int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestUsePoint);

            // then
            assertThat(usedPoints).isEqualTo(0);
        }
    }
} 
