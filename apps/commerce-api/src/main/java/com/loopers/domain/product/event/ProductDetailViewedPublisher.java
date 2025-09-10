package com.loopers.domain.product.event;

public interface ProductDetailViewedPublisher {
    void publish(ProductViewedEvent event);
}
