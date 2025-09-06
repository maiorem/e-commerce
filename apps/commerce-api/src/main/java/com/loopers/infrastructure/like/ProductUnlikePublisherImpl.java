package com.loopers.infrastructure.like;

import com.loopers.domain.like.event.ProductUnLikePublisher;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductUnlikePublisherImpl implements ProductUnLikePublisher {

    @Value("${kafka.topics.like-events}")
    private String likesTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductUnlikedEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(likesTopic, event.getProductId().toString(), event);
    }
}
