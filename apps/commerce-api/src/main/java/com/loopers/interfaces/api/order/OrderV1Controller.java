package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderApplicationService;
import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.PaymentFailedException;
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
            log.info("주문 생성 시작 - User: {}, Payment: {}", userId, request.paymentMethod());

            // 요청 검증
            request.validate();

            OrderCommand command = request.toCommand(userId);
            log.info("주문 명령 생성 완료 - Command: {}", command);

            // 주문 생성
            OrderInfo orderInfo = orderApplicationService.createOrder(command);
            log.info("주문 생성 완료 - OrderId: {}", orderInfo.orderId());

            // 결제 요청
            try {
                switch (request.paymentMethod()) {
                    case CREDIT_CARD -> {
                        log.info("카드 결제 처리 시작 - OrderId: {}", orderInfo.orderId());
                        try {
                            paymentApplicationService.processCardPayment(
                                    orderInfo, request.paymentMethod(), request.cardType(), request.cardNumber());
                            log.info("카드 결제 처리 완료 - OrderId: {}", orderInfo.orderId());
                        } catch (Exception e) {
                            log.error("카드 결제 처리 실패 - OrderId: {}, Error: {}", orderInfo.orderId(), e.getMessage(), e);
                            // 결제 실패 시 주문 취소 처리
                            paymentApplicationService.handlePaymentFailure(orderInfo.orderId());
                            throw new PaymentFailedException("카드 결제에 실패했습니다: " + e.getMessage());
                        }
                    }
                    case POINT -> {
                        log.info("포인트 결제 처리 시작 - OrderId: {}", orderInfo.orderId());
                        paymentApplicationService.processPointPayment(
                                orderInfo, request.paymentMethod(), request.pointAmount());
                        log.info("포인트 결제 처리 완료 - OrderId: {}", orderInfo.orderId());
                    }
                    default -> throw new IllegalArgumentException("지원하지 않는 결제 방법입니다: " + request.paymentMethod());
                }
            } catch (Exception e) {
                log.error("결제 처리 실패 - OrderId: {}, Error: {}", orderInfo.orderId(), e.getMessage(), e);
                // 결제 실패 시 주문을 취소 상태로 변경하거나 적절한 처리 필요
                return ApiResponse.fail("PAYMENT_FAILED", "결제 처리에 실패했습니다: " + e.getMessage(), OrderV1Dto.CreateOrderResponse.class);
            }

            OrderV1Dto.CreateOrderResponse response = OrderV1Dto.CreateOrderResponse.from(orderInfo);
            log.info("주문 생성 및 결제 완료 - OrderId: {}, Response: {}", orderInfo.orderId(), response);
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("주문 생성 실패 - User: {}, Error: {}", userId, e.getMessage(), e);
            return ApiResponse.fail("ORDER_CREATION_FAILED", "주문 생성에 실패했습니다: " + e.getMessage(), OrderV1Dto.CreateOrderResponse.class);
        }
    }
}
