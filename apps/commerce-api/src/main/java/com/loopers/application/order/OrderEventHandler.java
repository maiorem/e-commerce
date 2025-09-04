package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.order.event.OrderCeatedCouponReserveCommand;
import com.loopers.domain.order.event.OrderCreatedStockDeductionCommand;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
import com.loopers.domain.user.event.UserActionType;
import com.loopers.event.OrderCreatedEvent;
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
    private final UserActionTrackingPort userActionTrackingPort;
    private final CouponProcessor couponProcessor;
    private final StockDeductionProcessor stockDeductionProcessor;

    /**
     * 재고 차감 (커맨드)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void processStockDeduction(OrderCreatedStockDeductionCommand command) {
        if (command.orderItemList() != null && !command.orderItemList().isEmpty()) {
            try {
                log.info("[OrderEventHandler] 재고차감 처리 시작 - ");
                stockDeductionProcessor.deductProductStocks(command.orderItemList());
                log.info("[OrderEventHandler] 재고차감 처리 완료 - ");
            } catch (Exception e) {
                log.error("[OrderEventHandler] 재고차감 처리 실패 - ");
            }
        } else {
            log.debug("[OrderEventHandler] 차감할 재고가 없음");
        }

    }

    /**
     * 쿠폰 예약 (커맨드)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void processCouponReserve(OrderCeatedCouponReserveCommand command) {
        if (command.couponCode() != null && !command.couponCode().isEmpty()) {
            try {
                log.info("[OrderEventHandler] 쿠폰 예약 처리 시작 - OrderId: {}, CouponCode: {}",
                        command.orderId(), command.couponCode());

                couponProcessor.reserveCoupon(command.userId(), command.couponCode(), command.orderId());

                log.info("[OrderEventHandler] 쿠폰 예약 처리 완료 - OrderId: {}, CouponCode: {}",
                        command.orderId(), command.couponCode());

            } catch (Exception e) {
                log.error("[OrderEventHandler] 쿠폰 예약 처리 실패 - OrderId: {}, CouponCode: {}, Error: {}",
                        command.orderId(), command.couponCode(), e.getMessage(), e);

            }
        } else {
            log.debug("[OrderEventHandler] 예약할 쿠폰이 없음 - OrderId: {}", command.orderId());
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
            UserActionData actionData = UserActionData.create(
                    UserId.of(event.getUserId()),
                    UserActionType.ORDER_CREATE,
                    event.getOrderId()
            );
            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("주문 생성 행동 추적 실패", e);
        }
    }
}
