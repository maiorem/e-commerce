package com.loopers.application.event;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Map;

@Builder
@Getter
public class ProductMetricsAggregation {
    private final Map<Long, ViewCount> viewCounts;
    private final Map<Long, LikeUpdate> likeUpdates;
    private final Map<Long, SalesData> salesData;
    
    @Builder
    @Getter
    public static class ViewCount {
        private final Long productId;
        private final int count;
        private final ZonedDateTime lastViewedAt;
    }
    
    @Builder
    @Getter
    public static class LikeUpdate {
        private final Long productId;
        private final Long finalLikeCount;
        private final ZonedDateTime lastLikedAt;
    }
    
    @Builder
    @Getter
    public static class SalesData {
        private final Long productId;
        private final int salesCount;
        private final Long totalAmount;
    }
}