package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PointProcessor pointProcessor;
    private final CouponProcessor couponProcessor;
    private final StockDeductionProcessor stockDeductionProcessor;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentProcessor paymentProcessor;

    /**
     * 결제 요청
     */
    public void processPaymentAsync(OrderInfo orderInfo, PaymentMethod paymentMethod, CardType cardType, String cardNumber) {
        try {
            PaymentData payment = PaymentData.create(orderInfo.orderId(), paymentMethod, cardType, cardNumber, orderInfo.totalPrice(), orderInfo.userId());
            PaymentResult result = paymentGatewayPort.processPayment(payment);
            if (result.isSuccess()) {
                PaymentModel paymentInfo = PaymentModel.create(orderInfo.orderId(), paymentMethod, orderInfo.totalPrice(), result);
                paymentProcessor.save(paymentInfo);
            } else {
                log.error("결제 요청 실패: {}, 메시지: {}", result.transactionKey(), result.message());
            }
        } catch (Exception e) {
            log.error("결제 요청 접수 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 결제 콜백 처리
     */
    @Transactional
    public void handlePaymentCallback(String transactionId, PaymentStatus status, String reason) {

        PaymentModel payment = paymentRepository.findByTransactionKey(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("결제 내역이 존재하지 않습니다: " + transactionId));

        OrderModel order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 내역이 존재하지 않습니다: " + payment.getOrderId()));

        List<OrderItemModel> orderItems = orderItemRepository.findByOrderId(order.getId());

        if (status == PaymentStatus.SUCCESS) {
            order.confirm();
            payment.success();

            // 쿠폰 사용 처리
            couponProcessor.useCoupon(order.getUserId(), order.getCouponCode());

        } else {
            order.cancel();
            payment.fail();
            // 재고 복구
            stockDeductionProcessor.restoreProductStocks(orderItems);
            // 쿠폰 복구
            couponProcessor.restoreCoupon(order.getUserId(), order.getCouponCode());

        }

    }

}
