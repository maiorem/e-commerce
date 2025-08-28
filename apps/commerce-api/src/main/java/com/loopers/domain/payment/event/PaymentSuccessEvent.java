package com.loopers.domain.payment.event;

import com.loopers.domain.order.Money;
import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentSuccessEvent {

    private final Long orderId;
    private final Long paymentId;
    private final UserId userId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private final String transactionKey;
    private final String couponCode;
    private final ZonedDateTime occurredAt;

    public static PaymentSuccessEvent create(Long orderId, Long paymentId, UserId userId,
                                             Money amount, PaymentMethod paymentMethod,
                                             String transactionKey, String couponCode) {
        return new PaymentSuccessEvent(
                orderId, paymentId, userId, amount, paymentMethod,
                transactionKey, couponCode, ZonedDateTime.now()
        );
    }
}
