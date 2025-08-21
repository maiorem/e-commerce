package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderApplicationService;
import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderV1Controller implements OrderV1ApiSpec {

    private final PaymentApplicationService paymentApplicationService;
    private final OrderApplicationService orderApplicationService;

    @Override
    @PostMapping
    public ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(@RequestHeader("X-USER-ID") String userId, OrderV1Dto.CreateOrderRequest request) {
        OrderCommand command = request.toCommand(userId);

        // 주문 생성
        OrderInfo orderInfo = orderApplicationService.createOrder(command);

        // 결제 요청
        switch (request.paymentMethod()) {
            case CREDIT_CARD -> paymentApplicationService.processCardPayment(
                    orderInfo, request.paymentMethod(), request.cardType(), request.cardNumber());
            case POINT -> paymentApplicationService.processPointPayment(
                    orderInfo, request.paymentMethod(), request.pointAmount());
            default -> throw new IllegalArgumentException("지원하지 않는 결제 방법입니다: " + request.paymentMethod());
        }

        OrderV1Dto.CreateOrderResponse response = OrderV1Dto.CreateOrderResponse.from(orderInfo);
        return ApiResponse.success(response);
    }

}
