package com.loopers.infrastructure.like;

import com.loopers.domain.like.event.ProductLikePublisher;
import com.loopers.domain.like.event.ProductLikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductLikePublisherImpl implements ProductLikePublisher {

    @Value("${kafka.topics.like-events}")
    private String likesTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductLikedEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(likesTopic, event.getProductId().toString(), event);
    }
}
