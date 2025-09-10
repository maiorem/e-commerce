package com.loopers.domain.product.event;

public interface ProductChangedPublisher {
    void publishEvent(ProductViewedEvent event);
}
