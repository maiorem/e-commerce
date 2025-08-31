package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.event.PaymentSuccessEvent;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
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
public class PaymentSuccessEventHandler {

	private final OrderRepository orderRepository;
	private final CouponProcessor couponProcessor;
    private final DataPlatformPort dataPlatformPort;
    private final UserActionTrackingPort userActionTrackingPort;

	/**
	 * 결제 성공 - 주문 확정 이벤트 처리
	 */
    @Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handlePaymentSuccess(PaymentSuccessEvent event) {
		log.info("[PaymentEventHandler] 결제 성공 이벤트 처리 시작 - OrderId: {}, PaymentId: {}",
				event.getOrderId(), event.getPaymentId());

		try {
			// 주문 상태를 CONFIRMED로 변경
			OrderModel order = orderRepository.findById(event.getOrderId())
					.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId()));

			order.confirm();
			orderRepository.save(order);

			log.info("[PaymentEventHandler] 주문 확정 완료 - OrderId: {}", event.getOrderId());

		} catch (Exception e) {
			log.error("[PaymentEventHandler] 결제 성공 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
					event.getOrderId(), e.getMessage(), e);
		}
	}

	/**
	 * 결제 성공 - 쿠폰 사용 확정 이벤트 처리
	 */
	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void handlePaymentSuccessWithCouponConfirmation(PaymentSuccessEvent event) {
		log.info("[PaymentEventHandler] 결제 성공 - 쿠폰 사용 확정 이벤트 처리 시작 - OrderId: {}, PaymentId: {}",
				event.getOrderId(), event.getPaymentId());
		try {
            OrderModel order = orderRepository.findById(event.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId()));
            UserCouponModel myCoupon = couponProcessor.findByOrderId(event.getOrderId());
            couponProcessor.useCoupon(order.getUserId(), myCoupon.getCouponCode(), myCoupon.getOrderId());
			log.info("[PaymentEventHandler] 쿠폰 사용 확정 완료 - OrderId: {}", event.getOrderId());

		} catch (Exception e) {
			log.error("[PaymentEventHandler] 쿠폰 사용 확정 이벤트 처리 중 예외 발생 - OrderId: {}, Error: {}",
					event.getOrderId(), e.getMessage(), e);
		}
	}

    /**
     * 데이터 플랫폼 전송 처리
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processDataPlatformSending(PaymentSuccessEvent event) {
        try {
            log.info("📊 [OrderEventHandler] 데이터 플랫폼 전송 시작 - OrderId: {}", event.getOrderId());

            DataPlatformResult result = dataPlatformPort.sendPaymentSuccess(event);

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
     * 결제 성공 행동 추적
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackOrderCreationAction(PaymentSuccessEvent event) {
        try {
            OrderModel order = orderRepository.findById(event.getOrderId()).orElseThrow(
                    () -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + event.getOrderId())
            );
            UserActionData actionData = UserActionData.create(
                    order.getUserId(),
                    UserActionType.PAYMENT_SUCCESS,
                    event.getPaymentId()
            );
            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("결제 성공 행동 추적 실패", e);
        }
    }

}
