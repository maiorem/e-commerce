package com.loopers.domain.payment.event;

import org.springframework.stereotype.Component;

@Component
public interface PaymentSuccessPublisher {
    void publish(PaymentSuccessEvent event);
}
