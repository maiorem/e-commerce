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
    public boolean processPayment(OrderModel order, int finalPaymentAmount) {
        // 외부 결제 게이트웨이와 통신하여 결제를 처리하는 로직 구현


        return true;
    }

}
