package com.loopers.event;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class ProductViewedEvent extends BaseEvent {

    private final Long productId;
    private final String userId;

    public ProductViewedEvent(String eventId, Long productId, String userId, ZonedDateTime occurredAt) {
        super(eventId, occurredAt);
        this.productId = productId;
        this.userId = userId;
    }
}