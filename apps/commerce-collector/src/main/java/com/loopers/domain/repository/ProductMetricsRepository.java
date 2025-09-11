package com.loopers.domain.repository;

import com.loopers.domain.entity.ProductMetrics;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductMetricsRepository {
    Optional<ProductMetrics> findByProductId(Long productId);
    ProductMetrics save(ProductMetrics productMetrics);

    ProductMetrics upsertViewCount(Long productId, ZonedDateTime viewedAt);
    ProductMetrics upsertLikeCount(Long productId, Long newLikeCount, ZonedDateTime likedAt);
    ProductMetrics upsertSalesCount(Long productId, Long amount);
    
    List<ProductMetrics> findAllByLastUpdatedDate(LocalDate date);
    
    // 배치 처리 메서드들
    void incrementViewCountBatch(Long productId, int incrementCount, ZonedDateTime lastViewedAt);
    void updateLikeCountBatch(Long productId, Long finalCount, ZonedDateTime lastLikedAt);
    void incrementSalesCountBatch(Long productId, int salesCount, Long totalAmount);
}
