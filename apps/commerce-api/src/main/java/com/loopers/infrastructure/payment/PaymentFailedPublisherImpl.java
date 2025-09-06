package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentFailedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFailedPublisherImpl implements PaymentFailedPublisher {

    @Value("${kafka.topics.order-events}")
    private String orderTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentFailedEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(orderTopic, event.getOrderId().toString(), event);
    }
}
