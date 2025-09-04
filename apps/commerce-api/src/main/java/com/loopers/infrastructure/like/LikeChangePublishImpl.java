package com.loopers.infrastructure.like;

import com.loopers.domain.like.event.LikeChangePublisher;
import com.loopers.event.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LikeChangePublishImpl implements LikeChangePublisher {

    @Value("${kafka.topics.like-events}")
    private String likeTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(LikeChangedEvent event) {
        applicationEventPublisher.publishEvent(event);
        kafkaTemplate.send(likeTopic, String.valueOf(event.getProductId()), event);

    }
}
