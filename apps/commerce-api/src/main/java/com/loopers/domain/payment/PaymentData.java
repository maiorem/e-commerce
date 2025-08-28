package com.loopers.domain.payment;

import com.loopers.domain.order.Money;
import com.loopers.domain.payment.model.CardType;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.UserId;

public record PaymentData(
        Long orderId,
        PaymentMethod paymentMethod,
        CardType cardType,
        String cardNumber,
        Money finalTotalPrice,
        UserId userId
) {
    public static PaymentData create(Long orderId, PaymentMethod paymentMethod, CardType cardType, String cardNumber, Money finalTotalPrice, UserId userId) {
        return new PaymentData(orderId, paymentMethod, cardType, cardNumber, finalTotalPrice, userId);
    }
}
