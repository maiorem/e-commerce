package com.loopers.domain.product.event;

import com.loopers.event.ProductViewedEvent;

public interface ProductDetailViewedPublisher {
    void publish(ProductViewedEvent event);
}
