package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.event.PaymentRequestedEvent;
import com.loopers.domain.payment.event.PaymentRequestedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRequestedPublisherImpl implements PaymentRequestedPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentRequestedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
