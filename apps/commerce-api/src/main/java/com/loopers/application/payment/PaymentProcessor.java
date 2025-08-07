package com.loopers.application.payment;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.ExternalPaymentGatewayService;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final ExternalPaymentGatewayService externalPaymentGatewayService;

    public PaymentHistoryModel pay(OrderModel order, PaymentMethod method, int finalPaymentAmount) {
        boolean externalPaymentSuccess = externalPaymentGatewayService.processPayment(order, finalPaymentAmount);
        if (!externalPaymentSuccess) {
            throw new IllegalArgumentException("외부 결제 시스템 오류 또는 결제 실패");
        }

        PaymentHistoryModel paymentHistory = PaymentHistoryModel.complete(
                order.getId(),
                method,
                finalPaymentAmount
        );
        return paymentRepository.save(paymentHistory);
    }
}
