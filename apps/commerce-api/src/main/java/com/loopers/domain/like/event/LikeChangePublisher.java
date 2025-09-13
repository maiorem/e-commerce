package com.loopers.domain.like.event;

public interface LikeChangePublisher {
    void publish(LikeChangedEvent event);
}
