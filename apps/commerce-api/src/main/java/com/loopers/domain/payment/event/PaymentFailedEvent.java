package com.loopers.domain.payment.event;

import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentFailedEvent {

    private final Long orderId;
    private final PaymentMethod paymentMethod;
    private final String failureReason;
    private final ZonedDateTime occurredAt;

    public static PaymentFailedEvent create(PaymentModel payment,
                                            String failureReason) {
        return new PaymentFailedEvent(
                payment.getOrderId(), payment.getPaymentMethod(),
                failureReason,ZonedDateTime.now()
        );
    }
}
