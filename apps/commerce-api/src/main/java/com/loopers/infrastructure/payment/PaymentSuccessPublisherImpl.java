package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.event.PaymentSuccessEvent;
import com.loopers.domain.payment.event.PaymentSuccessPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSuccessPublisherImpl implements PaymentSuccessPublisher {

    @Value("${kafka.topics.order-events}")
    private String orderTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(PaymentSuccessEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(orderTopic, event.getOrderId().toString(), event);
    }
}
