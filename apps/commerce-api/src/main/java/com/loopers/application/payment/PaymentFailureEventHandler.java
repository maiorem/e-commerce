package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
import com.loopers.domain.user.event.UserActionType;
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
    private final DataPlatformPort dataPlatformPort;
    private final UserActionTrackingPort userActionTrackingPort;

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
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
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
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentFailedWithCouponRestoration(PaymentFailedEvent event) {

        try {
            OrderModel order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId()));

            UserCouponModel coupon = couponProcessor.findByOrderId(event.getOrderId());
            couponProcessor.restoreCoupon(order.getUserId(), coupon.getCouponCode());
            log.info("[PaymentEventHandler] 쿠폰 복구 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[PaymentEventHandler] 쿠폰 복구 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 데이터 플랫폼 전송 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processDataPlatformSending(PaymentFailedEvent event) {
        try {
            log.info("📊 [OrderEventHandler] 데이터 플랫폼 전송 시작 - OrderId: {}", event.getOrderId());

            DataPlatformResult result = dataPlatformPort.sendPaymentFailure(event);

            if (result.isSuccess()) {
                log.info("[OrderEventHandler] 데이터 플랫폼 전송 성공 - OrderId: {}",
                        event.getOrderId());
            } else {
                log.error("[OrderEventHandler] 데이터 플랫폼 전송 실패 - OrderId: {}, Error: {}",
                        event.getOrderId(), result.getMessage());

            }

        } catch (Exception e) {
            log.error("[OrderEventHandler] 데이터 플랫폼 전송 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    /**
     * 결제 성공 행동 추적
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackOrderCreationAction(PaymentFailedEvent event) {
        try {
            OrderModel order = orderRepository.findById(event.getOrderId()).orElseThrow(
                    () -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId())
            );
            UserActionData actionData = UserActionData.create(
                    order.getUserId(),
                    UserActionType.PAYMENT_FAILURE,
                    event.getOrderId()
            );
            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("결제 성공 행동 추적 실패", e);
        }
    }
}
