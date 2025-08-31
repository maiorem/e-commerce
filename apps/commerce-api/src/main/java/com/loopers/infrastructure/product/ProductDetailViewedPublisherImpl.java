package com.loopers.infrastructure.product;

import com.loopers.domain.product.event.ProductDetailViewedPublisher;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDetailViewedPublisherImpl implements ProductDetailViewedPublisher {

    @Value("${kafka.topics.catalog-events}")
    private String catalogTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void publish(ProductViewedEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(catalogTopic, event.getProductId().toString(), event);
    }
}
