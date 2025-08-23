package com.loopers.domain.payment;

public interface PaymentGatewayPort {
    PaymentResult processPayment(PaymentData paymentData);
    PaymentQueryResult queryPaymentStatus(String userId, String transactionKey);
    PaymentHistoryResult queryPaymentHistory(String userId, String orderId);
}
