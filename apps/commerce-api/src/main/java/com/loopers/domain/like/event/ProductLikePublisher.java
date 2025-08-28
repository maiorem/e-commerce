package com.loopers.domain.like.event;

public interface ProductLikePublisher {
    void publish(ProductLikedEvent event);
}
