package com.loopers.domain.product.event;

import org.springframework.stereotype.Component;

@Component
public interface ProductDetailViewedPublisher {
    void publish(ProductViewedEvent event);
}
