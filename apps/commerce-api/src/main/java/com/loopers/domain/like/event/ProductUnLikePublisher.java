package com.loopers.domain.like.event;

public interface ProductUnLikePublisher {
    void publish(ProductUnlikedEvent event);
}
