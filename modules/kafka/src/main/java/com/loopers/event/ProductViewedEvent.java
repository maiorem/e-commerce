package com.loopers.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class ProductViewedEvent {

    private final String eventId;
    private final Long productId;
    private final String userId;
    private final ZonedDateTime occurredAt;

    public static ProductViewedEvent createDetailView(Long productId, String userId) {
        return new ProductViewedEvent(
                "product-viewed-" + productId + "-" + System.currentTimeMillis(),
                productId,
                userId,
                ZonedDateTime.now()
        );
    }
}
