package com.loopers.domain.payment;

import com.loopers.domain.user.UserId;

public record PaymentData(
        Long orderId,
        PaymentMethod paymentMethod,
        CardType cardType,
        String cardNumber,
        int finalTotalPrice,
        UserId userId) {

    public static PaymentData create(Long orderId, PaymentMethod paymentMethod, CardType cardType, String cardNumber, int finalTotalPrice, UserId userId) {
        return new PaymentData(orderId, paymentMethod, cardType, cardNumber, finalTotalPrice, userId);
    }
}
