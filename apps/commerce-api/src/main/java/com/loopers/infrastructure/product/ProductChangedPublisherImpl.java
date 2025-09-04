package com.loopers.infrastructure.product;

import com.loopers.domain.product.event.ProductChangedPublisher;
import com.loopers.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductChangedPublisherImpl implements ProductChangedPublisher {

    @Value("${kafka.topics.view-events}")
    private String viewTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishEvent(ProductViewedEvent event) {
        eventPublisher.publishEvent(event);
        kafkaTemplate.send(viewTopic, event.getProductId().toString(), event);
    }
}
