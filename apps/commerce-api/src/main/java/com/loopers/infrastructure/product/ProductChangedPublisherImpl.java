package com.loopers.infrastructure.product;

import com.loopers.domain.product.event.ProductChangedPublisher;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductChangedPublisherImpl implements ProductChangedPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishEvent(ProductViewedEvent event) {
        eventPublisher.publishEvent(event);
    }
}
