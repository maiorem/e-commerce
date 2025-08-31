package com.loopers.domain.payment.event;

public interface PaymentRequestedPublisher {
    void publish(PaymentRequestedEvent event);
}
