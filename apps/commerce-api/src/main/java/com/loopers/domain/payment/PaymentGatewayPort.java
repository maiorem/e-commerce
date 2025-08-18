package com.loopers.domain.payment;

public interface PaymentGatewayPort {
    PaymentResult processPayment(PaymentData paymentData);
}
