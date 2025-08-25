package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final DataPlatformPort dataPlatformPort;
    private final CouponProcessor couponProcessor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[OrderEventHandler] 주문 생성 이벤트 처리 시작 - OrderId: {}", event.getOrderId());

        try {
            // 1. 쿠폰 예약 처리
            processCouponUsage(event);

            // 2. 데이터 플랫폼으로 주문 정보 전송
            processDataPlatformSending(event);

            // 3. 사용자 행동 추적
            trackOrderCreationAction(event);

            log.info("[OrderEventHandler] 주문 후속 처리 완료 - OrderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("[OrderEventHandler] 주문 생성 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);

        }
    }

    /**
     * 쿠폰 예약
     */
    private void processCouponUsage(OrderCreatedEvent event) {
        if (event.getCouponCode() != null && !event.getCouponCode().isEmpty()) {
            try {
                log.info("[OrderEventHandler] 쿠폰 예약 처리 시작 - OrderId: {}, CouponCode: {}",
                        event.getOrderId(), event.getCouponCode());

                couponProcessor.reserveCoupon(event.getUserId(), event.getCouponCode());

                log.info("[OrderEventHandler] 쿠폰 예약 처리 완료 - OrderId: {}, CouponCode: {}",
                        event.getOrderId(), event.getCouponCode());

            } catch (Exception e) {
                log.error("[OrderEventHandler] 쿠폰 예약 처리 실패 - OrderId: {}, CouponCode: {}, Error: {}",
                        event.getOrderId(), event.getCouponCode(), e.getMessage(), e);

            }
        } else {
            log.debug("[OrderEventHandler] 예약할 쿠폰이 없음 - OrderId: {}", event.getOrderId());
        }
    }

    /**
     * 데이터 플랫폼 전송 처리
     */
    private void processDataPlatformSending(OrderCreatedEvent event) {
        try {
            log.info("📊 [OrderEventHandler] 데이터 플랫폼 전송 시작 - OrderId: {}", event.getOrderId());

            DataPlatformResult result = dataPlatformPort.sendOrderData(event);

            if (result.isSuccess()) {
                log.info("[OrderEventHandler] 데이터 플랫폼 전송 성공 - OrderId: {}, TransactionKey: {}",
                        event.getOrderId(), result.getTransactionKey());
            } else {
                log.error("[OrderEventHandler] 데이터 플랫폼 전송 실패 - OrderId: {}, Error: {}",
                        event.getOrderId(), result.getMessage());

            }

        } catch (Exception e) {
            log.error("[OrderEventHandler] 데이터 플랫폼 전송 중 예외 발생 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
        }
    }

    private void trackOrderCreationAction(OrderCreatedEvent event) {
        try {
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.ORDER_CREATE,
                    event.getOrderId(),
                    "amount=" + event.getTotalAmount().getAmount() +
                            ",payment_method=" + event.getPaymentMethod().name()
            );

            // DataPlatformPort를 통해 사용자 행동도 전송
            dataPlatformPort.sendUserActionData(
                    event.getUserId(),
                    "ORDER_CREATE",
                    event.getOrderId(),
                    event.getOccurredAt()
            );

        } catch (Exception e) {
            log.error("주문 생성 행동 추적 실패", e);
        }
    }
}
