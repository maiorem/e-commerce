package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final DataPlatformPort dataPlatformPort;
    private final CouponProcessor couponProcessor;

    /**
     * 쿠폰 예약
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processCouponReserve(OrderCreatedEvent event) {
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
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processDataPlatformSending(OrderCreatedEvent event) {
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

    /**
     * 주문 생성 행동 추적
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackOrderCreationAction(OrderCreatedEvent event) {
        try {
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
