package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailureEventHandler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockDeductionProcessor stockDeductionProcessor;
    private final CouponProcessor couponProcessor;

    /**
     * 결제 실패 - 주문 취소 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailed(PaymentFailedEvent event) {

        try {
            // 주문 조회
            OrderModel order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId()));

            // 주문 상태를 CANCELLED로 변경
            order.cancel();
            orderRepository.save(order);
            log.info("[PaymentEventHandler] 주문 상태 취소 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[PaymentEventHandler] 주문 취소 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 결제 실패 - 재고 복구 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailedWithStockRestoration(PaymentFailedEvent event) {

        try {
            // 주문 아이템 조회
            List<OrderItemModel> orderItems = orderItemRepository.findByOrderId(event.getOrderId());

            // 재고 복구
            stockDeductionProcessor.restoreProductStocks(orderItems);
            log.info("[PaymentEventHandler] 재고 복구 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[PaymentEventHandler] 재고 복구 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 결제 실패 - 쿠폰 복구 이벤트 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentFailedWithCouponRestoration(PaymentFailedEvent event) {

        try {
            couponProcessor.restoreCoupon(event.getUserId(), event.getCouponCode());
            log.info("[PaymentEventHandler] 쿠폰 복구 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[PaymentEventHandler] 쿠폰 복구 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}
