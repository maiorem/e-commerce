package com.loopers.domain.payment.event;

import com.loopers.domain.order.Money;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentRequestedEvent {

    private final Long orderId;
    private final UserId userId;
    private final Money totalAmount;
    private final PaymentMethod paymentMethod;
    private final String couponCode;

    private final CardType cardType;
    private final String cardNumber;

    private final Integer pointAmount;

    private final ZonedDateTime occurredAt;

    public static PaymentRequestedEvent forCard(Long orderId, UserId userId, Money totalAmount,
                                                PaymentMethod paymentMethod, CardType cardType, String cardNumber,
                                                String couponCode) {
        return new PaymentRequestedEvent(orderId, userId, totalAmount, paymentMethod, couponCode, cardType, cardNumber, null, ZonedDateTime.now());
    }

    public static PaymentRequestedEvent forPoint(Long orderId, UserId userId, Money totalAmount,
                                                 PaymentMethod paymentMethod, Integer pointAmount, String couponCode) {
        return new PaymentRequestedEvent(orderId, userId, totalAmount, paymentMethod, couponCode, null, null, pointAmount, ZonedDateTime.now());
    }
}


