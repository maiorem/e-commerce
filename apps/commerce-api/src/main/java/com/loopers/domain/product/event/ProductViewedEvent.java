package com.loopers.domain.product.event;

import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class ProductViewedEvent {

    private final Long productId;
    private final UserId userId;
    private final ViewType viewType;
    private final String referrer; // 어디서 왔는지
    private final ZonedDateTime occurredAt;

    public static ProductViewedEvent createDetailView(Long productId, UserId userId) {
        return new ProductViewedEvent(productId, userId, ViewType.DETAIL, null, ZonedDateTime.now());
    }

    public static ProductViewedEvent createListView(Long productId, UserId userId, String referrer) {
        return new ProductViewedEvent(productId, userId, ViewType.LIST, referrer, ZonedDateTime.now());
    }

    public static ProductViewedEvent createSearchView(Long productId, UserId userId, String searchQuery) {
        return new ProductViewedEvent(productId, userId, ViewType.SEARCH, searchQuery, ZonedDateTime.now());
    }

}
