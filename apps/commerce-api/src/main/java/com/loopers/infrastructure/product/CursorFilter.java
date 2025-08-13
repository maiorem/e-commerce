package com.loopers.infrastructure.product;

import java.time.ZonedDateTime;

public record CursorFilter(Long lastId, int lastLikesCount, int lastPrice, ZonedDateTime lastCreatedAt) {

    public static CursorFilter from(Long lastId, int lastLikesCount, int lastPrice, ZonedDateTime lastCreatedAt) {
        return new CursorFilter(lastId, lastLikesCount, lastPrice, lastCreatedAt);
    }
}
