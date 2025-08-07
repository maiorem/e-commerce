package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.payment.PaymentProcessor;
import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.application.user.UserValidator;
import com.loopers.domain.order.OrderCreationDomainService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final UserValidator userValidator;

    private final OrderItemProductsValidator orderItemProductsValidator;

    private final PointProcessor pointProcessor;

    private final StockDeductionProcessor stockDeductionProcessor;

    private final PaymentProcessor paymentProcessor;

    private final OrderPersistenceHandler orderPersistenceHandler;

    private final CouponProcessor couponProcessor;

    private final OrderCreationDomainService orderCreationDomainService;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderInfo createOrder(OrderCommand command) {

        // 1. 사용자 존재 여부 확인
        userValidator.validateUserExists(command.userId());

        // 2. 주문 아이템 상품 유효성 검증 및 상품 목록 조회
        List<ProductModel> products = orderItemProductsValidator.validateAndGetProducts(command.items());
        List<OrderItemModel> orderItems = OrderItemCommand.convertToOrderItems(command.items());

        // 3. 총액 계산
        int orderPrice = orderCreationDomainService.calculateOrderPrice(orderItems);

        // -- 쿠폰 할인 적용 --
        int currentProcessingAmount = couponProcessor.applyCouponDiscount(command.userId(), orderPrice, command.couponCode());

        // 4. 사용 요청한 포인트 처리
        int usedPoints = pointProcessor.processPointUsage(command.userId(), currentProcessingAmount, command.requestPoint());
        currentProcessingAmount -= usedPoints;

        // 5. 최종 결제 금액 계산 (주문 총액 - 쿠폰 할인 처리 - 사용 포인트)
        int finalTotalPrice = currentProcessingAmount;

        // 6. 재고 차감
        stockDeductionProcessor.deductProductStocks(orderItems);

        // 7. 주문 생성 저장 (PENDING)
        OrderModel order = OrderModel.create(command.userId(), finalTotalPrice);
        orderPersistenceHandler.saveOrder(order);

        // 8. 외부 결제 연동 처리
        PaymentHistoryModel paymentHistory = paymentProcessor.pay(order, command.paymentMethod(), finalTotalPrice);

        // 9. 주문 완료 처리 (COMPLETED)
        order.complete();

        // -- 쿠폰 사용 처리 --
        couponProcessor.useCoupon(command.userId(), command.couponCode());

        // 10. 주문 정보 및 주문 아이템 저장
        List<OrderItemModel> savedOrderItems = orderPersistenceHandler.saveOrderItemAndPaymentHistory(order, orderItems, paymentHistory);

        // 11. 최종 응답 DTO 변환 및 반환
        List<OrderItemInfo> orderItemInfos = OrderItemInfo.createOrderItemInfos(savedOrderItems, products);
        return OrderInfo.from(order, orderItemInfos);
    }


} 
