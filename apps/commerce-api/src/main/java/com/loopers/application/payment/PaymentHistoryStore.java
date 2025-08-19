package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentHistoryStore {

    private final PaymentRepository paymentRepository;

    public PaymentHistoryModel savePaymentHistory(PaymentHistoryModel paymentHistory) {
        return paymentRepository.save(paymentHistory);
    }
}
