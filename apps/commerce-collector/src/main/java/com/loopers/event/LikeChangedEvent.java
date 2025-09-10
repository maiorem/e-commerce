package com.loopers.event;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class LikeChangedEvent extends BaseEvent {

    private final Long productId;
    private final String userId;
    private final String changeType; // LIKE or UNLIKE
    private final int oldLikeCount;
    private final int newLikeCount;
    private final int version;

    public LikeChangedEvent(String eventId, Long productId, String userId, String changeType,
                           int oldLikeCount, int newLikeCount, int version, ZonedDateTime occurredAt) {
        super(eventId, occurredAt);
        this.productId = productId;
        this.userId = userId;
        this.changeType = changeType;
        this.oldLikeCount = oldLikeCount;
        this.newLikeCount = newLikeCount;
        this.version = version;
    }
}