package com.loopers.application.payment;

import com.loopers.domain.payment.model.PaymentModel;
import com.loopers.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;

    public PaymentModel save(PaymentModel paymentHistory) {
        return paymentRepository.save(paymentHistory);
    }
}
