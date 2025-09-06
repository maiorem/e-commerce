package com.loopers.domain.like.event;

import com.loopers.event.LikeChangedEvent;

public interface LikeChangePublisher {
    void publish(LikeChangedEvent event);
}
