package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderApplicationService;
import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderV1Controller implements OrderV1ApiSpec {

    private final PaymentApplicationService paymentApplicationService;
	private final OrderApplicationService orderApplicationService;

	@Override
	@PostMapping
	public ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(@RequestHeader("X-USER-ID") String userId, @RequestBody OrderV1Dto.CreateOrderRequest request) {
		try {
			request.validate();
			OrderCommand command = request.toCommand(userId);

			// 주문 생성
            OrderInfo orderInfo = orderApplicationService.createOrder(command);

            // 결제 요청
            try {
                switch (request.paymentMethod()) {
                    case CREDIT_CARD -> {
                            paymentApplicationService.processCardPayment(
                                    orderInfo, request.paymentMethod(), request.cardType(), request.cardNumber());
                    }
                    case POINT -> {
                        paymentApplicationService.processPointPayment(
                                orderInfo, request.paymentMethod(), request.pointAmount(), request.couponCode());
                    }
                    default -> throw new IllegalArgumentException("지원하지 않는 결제 방법입니다: " + request.paymentMethod());
                }
            } catch (Exception e) {
                log.error("결제 처리 실패 - OrderId: {}, Error: {}", orderInfo.orderId(), e.getMessage(), e);
                throw new CoreException(ErrorType.PAYMENT_FAILED, "결제 처리에 실패했습니다: " + e.getMessage());
            }
            OrderV1Dto.CreateOrderResponse response = OrderV1Dto.CreateOrderResponse.from(orderInfo);
			return ApiResponse.success(response);

		} catch (Exception e) {
			log.error("주문 생성 실패 - User: {}, Error: {}", userId, e.getMessage(), e);
            throw new CoreException(ErrorType.ORDER_CREATION_FAILED, "주문 생성에 실패했습니다: " + e.getMessage());
		}
	}
}
