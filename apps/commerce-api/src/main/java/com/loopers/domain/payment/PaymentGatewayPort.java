package com.loopers.domain.payment;

public interface PaymentGatewayPort {
    PaymentResult processPayment(PaymentData paymentData);
    PaymentQueryResult queryPaymentStatus(String transactionKey);
    PaymentHistoryResult queryPaymentHistory(String orderId);
}
