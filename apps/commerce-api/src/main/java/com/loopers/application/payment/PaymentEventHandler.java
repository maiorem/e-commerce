package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CouponProcessor couponProcessor;
    private final StockDeductionProcessor stockDeductionProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("✅ [PaymentEventHandler] 결제 성공 이벤트 처리 시작 - OrderId: {}, PaymentId: {}",
                event.getOrderId(), event.getPaymentId());

        try {
            // 주문 상태를 CONFIRMED로 변경
            OrderModel order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId()));

            // 쿠폰 사용 확정 처리
            couponProcessor.useCoupon(event.getUserId(), order.getCouponCode());

            order.confirm();
            orderRepository.save(order);

            log.info("✅ [PaymentEventHandler] 주문 확정 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("💥 [PaymentEventHandler] 결제 성공 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("❌ [PaymentEventHandler] 결제 실패 이벤트 처리 시작 - OrderId: {}, Reason: {}",
                event.getOrderId(), event.getFailureReason());

        try {
            // 주문 조회
            OrderModel order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId()));

            // 주문 상태를 CANCELLED로 변경
            order.cancel();
            orderRepository.save(order);

            // 주문 아이템 조회
            List<OrderItemModel> orderItems = orderItemRepository.findByOrderId(event.getOrderId());

            // 재고 복구
            stockDeductionProcessor.restoreProductStocks(orderItems);
            log.info("🔄 [PaymentEventHandler] 재고 복구 완료 - OrderId: {}", event.getOrderId());

            // 쿠폰 예약 취소
            if (order.getCouponCode() != null && !order.getCouponCode().isEmpty()) {
                couponProcessor.restoreCoupon(event.getUserId(), order.getCouponCode());
                log.info("🎫 [PaymentEventHandler] 쿠폰 복구 완료 - OrderId: {}, CouponCode: {}",
                        event.getOrderId(), order.getCouponCode());
            }

            log.info("✅ [PaymentEventHandler] 결제 실패 복구 처리 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[PaymentEventHandler] 결제 실패 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }
}
