package com.loopers.infrastructure.order;

import com.loopers.domain.order.event.OrderCreatedPublisher;
import com.loopers.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedPublisherImpl implements OrderCreatedPublisher {

    @Value("${kafka.topics.order-events}")
    private String orderTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(orderTopic, event.getOrderId().toString(), event);
    }
}
