package com.loopers.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class ProductClickedEvent {

    private final Long productId;
    private final String userId;
    private final ClickContext clickContext;
    private final Integer position; // 목록에서의 위치
    private final ZonedDateTime occurredAt;

    public static ProductClickedEvent create(Long productId, String userId, ClickContext clickContext) {
        return new ProductClickedEvent(productId, userId, clickContext, null, ZonedDateTime.now());
    }

    public static ProductClickedEvent create(Long productId, String userId, ClickContext clickContext, Integer position) {
        return new ProductClickedEvent(productId, userId, clickContext, position, ZonedDateTime.now());
    }

}
