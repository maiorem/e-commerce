package com.loopers.domain.payment.event;

import com.loopers.domain.payment.model.PaymentMethod;
import com.loopers.domain.payment.model.PaymentModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class PaymentSuccessEvent {

    private final Long orderId;
    private final Long paymentId;
    private final PaymentMethod paymentMethod;
    private final String transactionKey;
    private final ZonedDateTime occurredAt;

    public static PaymentSuccessEvent create(PaymentModel payment,
                                             String transactionKey) {
        return new PaymentSuccessEvent(
                payment.getOrderId(), payment.getId(), payment.getPaymentMethod(), transactionKey, ZonedDateTime.now()
        );
    }
}
