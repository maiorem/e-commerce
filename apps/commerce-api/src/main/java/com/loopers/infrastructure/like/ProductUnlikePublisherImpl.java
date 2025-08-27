package com.loopers.infrastructure.like;

import com.loopers.domain.like.event.ProductUnLikePublisher;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductUnlikePublisherImpl implements ProductUnLikePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ProductUnlikedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
