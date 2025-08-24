package com.loopers.domain.like.event;

import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class ProductUnlikedEvent {

    private final Long productId;
    private final UserId userId;
    private final ZonedDateTime occurredAt;

    public static ProductUnlikedEvent create(Long productId, UserId userId) {
        return new ProductUnlikedEvent(productId, userId, ZonedDateTime.now());
    }

}
