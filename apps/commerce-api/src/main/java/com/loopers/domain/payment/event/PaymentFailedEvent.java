package com.loopers.domain.payment.event;

import com.loopers.domain.order.Money;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentFailedEvent {

    private final Long orderId;
    private final UserId userId;
    private final Money amount;
    private final PaymentMethod paymentMethod;
    private final String failureReason;
    private final String couponCode;
    private final ZonedDateTime occurredAt;

    public static PaymentFailedEvent create(Long orderId, UserId userId,
                                            Money amount, PaymentMethod paymentMethod,
                                            String failureReason, String couponCode) {
        return new PaymentFailedEvent(
                orderId, userId, amount, paymentMethod,
                failureReason, couponCode, ZonedDateTime.now()
        );
    }
}
