package com.loopers.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class LikeChangedEvent {

    private final String eventId;
    private final Long productId;
    private final String userId;
    private final LikeChangeType changeType;
    private final int oldLikeCount;
    private final int newLikeCount;
    private final ZonedDateTime occurredAt;
    private final int version;

    public static LikeChangedEvent liked(Long productId, String userId, int oldCount, int newCount) {
        return new LikeChangedEvent(
                "like-changed-" + productId + "-" + System.currentTimeMillis(),
                productId,
                userId,
                LikeChangeType.LIKE,
                oldCount,
                newCount,
                ZonedDateTime.now(),
                1
        );
    }

    public static LikeChangedEvent unliked(Long productId, String userId, int oldCount, int newCount) {
        return new LikeChangedEvent(
                "like-changed-" + productId + "-" + System.currentTimeMillis(),
                productId,
                userId,
                LikeChangeType.UNLIKE,
                oldCount,
                newCount,
                ZonedDateTime.now(),
                1
        );
    }
}
