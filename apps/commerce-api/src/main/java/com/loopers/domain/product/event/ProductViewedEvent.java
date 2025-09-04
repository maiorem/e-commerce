package com.loopers.domain.product.event;

import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class ProductViewedEvent {

    private final String eventId;
    private final Long productId;
    private final UserId userId;
    private final ViewType viewType;
    private final String referrer;
    private final String userAgent; // 집계용
    private final ZonedDateTime occurredAt;
    private final int version;

    public static ProductViewedEvent createDetailView(Long productId, UserId userId) {
        return new ProductViewedEvent(
                "product-viewed-" + productId + "-" + System.currentTimeMillis(),
                productId,
                userId,
                ViewType.DETAIL,
                null,
                "WEB",
                ZonedDateTime.now(),
                1
        );
    }

    public static ProductViewedEvent createListView(Long productId, UserId userId, String referrer) {
        return new ProductViewedEvent(
                "product-list-viewed-" + System.currentTimeMillis(),
                productId,
                userId,
                ViewType.LIST,
                referrer,
                "WEB",
                ZonedDateTime.now(),
                1
        );
    }

    public static ProductViewedEvent createSearchView(Long productId, UserId userId, String searchQuery) {
        return new ProductViewedEvent(
                "product-searched-" + productId + "-" + System.currentTimeMillis(),
                productId,
                userId,
                ViewType.LIST,
                searchQuery,
                "WEB",
                ZonedDateTime.now(),
                1);
    }

}
