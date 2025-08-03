package com.loopers.domain.payment;

import com.loopers.domain.order.OrderModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExternalPaymentGatewayService {

    /**
     * 외부 결제 게이트웨이와의 통신을 통해 결제 요청을 처리합니다.
     */
    public PaymentHistoryModel processPayment(OrderModel order) {
        // 외부 결제 게이트웨이와 통신하여 결제를 처리하는 로직 구현
        // 예: HTTP 클라이언트를 사용하여 API 호출

        return PaymentHistoryModel.of(order.getId(), PaymentMethod.CREDIT_CARD, order.getTotalAmount());
    }

}
