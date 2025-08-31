package com.loopers.domain.payment.event;

import org.springframework.stereotype.Component;

@Component
public interface PaymentFailedPublisher {
    void  publish(PaymentFailedEvent event);
}
