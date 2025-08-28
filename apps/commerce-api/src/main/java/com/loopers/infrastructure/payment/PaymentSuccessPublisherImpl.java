package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.event.PaymentSuccessEvent;
import com.loopers.domain.payment.event.PaymentSuccessPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSuccessPublisherImpl implements PaymentSuccessPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentSuccessEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
