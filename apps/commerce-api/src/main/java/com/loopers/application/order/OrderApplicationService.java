package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.application.user.UserValidator;
import com.loopers.domain.order.Money;
import com.loopers.domain.order.OrderCreationDomainService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final UserValidator userValidator;

    private final OrderItemProductsValidator orderItemProductsValidator;

    private final PointProcessor pointProcessor;

    private final StockDeductionProcessor stockDeductionProcessor;

    private final OrderPersistenceHandler orderPersistenceHandler;

    private final CouponProcessor couponProcessor;

    private final OrderCreationDomainService orderCreationDomainService;

    /**
     * 주문 생성
     */
    public OrderInfo createOrder(OrderCommand command) {
        // 1. 사용자 검증
        userValidator.validateUserExists(command.userId());

        // 2. 상품 검증 및 조회
        List<ProductModel> products = orderItemProductsValidator.validateAndGetProducts(command.items());
        List<OrderItemModel> orderItems = OrderItemCommand.convertToOrderItems(command.items());

        // 3. 가격 계산
        int orderPrice = orderCreationDomainService.calculateOrderPrice(orderItems);
        int currentProcessingAmount = couponProcessor.applyCouponDiscount(command.userId(), orderPrice, command.couponCode());
        int usedPoints = pointProcessor.processPointUsage(command.userId(), currentProcessingAmount, command.requestPoint());
        int finalTotalPrice = currentProcessingAmount - usedPoints;

        // 4. 재고 차감
        stockDeductionProcessor.deductProductStocks(orderItems);

        // 5. 주문 생성 및 저장 (CREATED 상태) - 저장된 엔티티 사용
        OrderModel order = OrderModel.create(command.userId(), Money.of(finalTotalPrice), command.couponCode(), command.paymentMethod());
        OrderModel savedOrder = orderPersistenceHandler.saveOrder(order);

        // 6. 쿠폰 예약
        couponProcessor.reserveCoupon(command.userId(), command.couponCode());

        // 7. 주문 아이템 저장 - 저장된 엔티티 사용
        List<OrderItemModel> savedOrderItems = orderPersistenceHandler.saveOrderItem(savedOrder, orderItems);

        return OrderInfo.from(savedOrder, OrderItemInfo.createOrderItemInfos(savedOrderItems, products));
    }

} 
