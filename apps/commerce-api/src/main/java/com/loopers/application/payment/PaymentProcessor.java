package com.loopers.application.payment;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final ExternalPaymentGatewayService externalPaymentGatewayService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentHistoryModel pay(OrderModel order, PaymentMethod method, int finalPaymentAmount) {
        boolean externalPaymentSuccess = externalPaymentGatewayService.processPayment(order, finalPaymentAmount);
        if (!externalPaymentSuccess) {
            throw new CoreException(ErrorType.BAD_REQUEST, "외부 결제 시스템 오류 또는 결제 실패");
        }

        PaymentHistoryModel paymentHistory = PaymentHistoryModel.complete(
                order.getId(),
                method,
                finalPaymentAmount
        );
        return paymentRepository.save(paymentHistory);
    }
}
