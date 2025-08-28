package com.loopers.infrastructure.like;

import com.loopers.domain.like.event.ProductLikePublisher;
import com.loopers.domain.like.event.ProductLikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductLikePublisherImpl implements ProductLikePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductLikedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
