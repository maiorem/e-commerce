package com.loopers.infrastructure.product;

import com.loopers.domain.product.event.ProductClickedEvent;
import com.loopers.domain.product.event.ProductClickedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductClickedPublishImpl implements ProductClickedPublisher {

    @Value("${kafka.topics.view-events}")
    private String viewTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(ProductClickedEvent event) {
        eventPublisher.publishEvent(event);
        kafkaTemplate.send(viewTopic, String.valueOf(event.getProductId()), event);
    }
}
