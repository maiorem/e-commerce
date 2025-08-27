package com.loopers.domain.product.event;

import org.springframework.stereotype.Component;

@Component
public interface ProductClickedPublisher {
    void publish(ProductClickedEvent event);
}
