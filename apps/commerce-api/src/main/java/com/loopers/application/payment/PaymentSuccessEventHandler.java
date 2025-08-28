package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.event.PaymentSuccessEvent;
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

}
