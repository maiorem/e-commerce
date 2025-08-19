package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.payment.PaymentHistoryStore;
import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.application.user.UserValidator;
import com.loopers.domain.order.OrderCreationDomainService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.PaymentData;
import com.loopers.domain.payment.PaymentGatewayPort;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentResult;
import com.loopers.domain.product.ProductModel;
import com.loopers.support.error.PaymentFailedException;
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

    private final PaymentGatewayPort paymentGatewayPort;

    private final OrderPersistenceHandler orderPersistenceHandler;

    private final CouponProcessor couponProcessor;

    private final PaymentHistoryStore paymentHistoryStore;

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

        // 7. 주문 생성 저장
        OrderModel order = OrderModel.create(command.userId(), finalTotalPrice, command.couponCode(), usedPoints);
        orderPersistenceHandler.saveOrder(order);

        // -- 쿠폰 사용예약  --
        couponProcessor.reserveCoupon(command.userId(), command.couponCode());

        // 8. 외부 결제 연동 처리
        PaymentData payment = PaymentData.create(order.getId(), command.paymentMethod(), command.cardType(), command.cardNumber(), finalTotalPrice, command.userId());
        PaymentResult result = paymentGatewayPort.processPayment(payment);

        if (result.isSuccess()) {
            // 9. 주문 정보 및 주문 아이템 저장 (PENDING)
            order.pending(result.transactionKey());
            List<OrderItemModel> savedOrderItems = orderPersistenceHandler.saveOrderItemAndPaymentHistory(order, orderItems);

            // 10. 결제 결과 저장
            PaymentHistoryModel paymentHistory = PaymentHistoryModel.of(order.getId(), command.paymentMethod(), finalTotalPrice, result);
            paymentHistoryStore.savePaymentHistory(paymentHistory);

            // 11. 최종 응답 DTO 변환 및 반환
            List<OrderItemInfo> orderItemInfos = OrderItemInfo.createOrderItemInfos(savedOrderItems, products);
            return OrderInfo.from(order, orderItemInfos);
        } else {
            // 재고 복구
            stockDeductionProcessor.restoreProductStocks(orderItems);
            // 쿠폰 복구
            couponProcessor.restoreCoupon(command.userId(), command.couponCode());
            // 포인트 복구
            pointProcessor.restorePoint(command.userId(), usedPoints);

            // 10. 결제 실패 히스토리 저장
            PaymentHistoryModel paymentHistory = PaymentHistoryModel.of(order.getId(), command.paymentMethod(), finalTotalPrice, result);
            paymentHistoryStore.savePaymentHistory(paymentHistory);

            // 11. 결제 실패 예외 발생
            throw new PaymentFailedException("결제에 실패했습니다.");
        }

    }

} 
