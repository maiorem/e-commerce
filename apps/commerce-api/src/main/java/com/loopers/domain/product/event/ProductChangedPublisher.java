package com.loopers.domain.product.event;

import com.loopers.event.ProductViewedEvent;

public interface ProductChangedPublisher {
    void publishEvent(ProductViewedEvent event);
}
