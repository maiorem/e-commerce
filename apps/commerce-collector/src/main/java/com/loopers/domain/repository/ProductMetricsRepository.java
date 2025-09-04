package com.loopers.domain.repository;

import com.loopers.domain.entity.ProductMetrics;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface ProductMetricsRepository {
    Optional<ProductMetrics> findByProductId(Long productId);
    ProductMetrics save(ProductMetrics productMetrics);

    ProductMetrics upsertViewCount(Long productId, ZonedDateTime viewedAt);
    ProductMetrics upsertLikeCount(Long productId, Long newLikeCount, ZonedDateTime likedAt);
}
