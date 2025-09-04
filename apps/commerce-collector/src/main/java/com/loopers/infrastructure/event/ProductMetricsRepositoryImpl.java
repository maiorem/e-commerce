package com.loopers.infrastructure.event;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.repository.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository jpaRepository;

    @Override
    public Optional<ProductMetrics> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public ProductMetrics save(ProductMetrics productMetrics) {
        return jpaRepository.save(productMetrics);
    }

    @Override
    public ProductMetrics upsertViewCount(Long productId, ZonedDateTime viewedAt) {
        ProductMetrics metrics = findByProductId(productId)
                .orElse(ProductMetrics.of(productId));

        metrics.incrementViewCount(viewedAt);
        return save(metrics);
    }

    @Override
    public ProductMetrics upsertLikeCount(Long productId, Long newLikeCount, ZonedDateTime likedAt) {
        ProductMetrics metrics = findByProductId(productId)
                .orElse(ProductMetrics.of(productId));

        metrics.updateLikeCount(newLikeCount, likedAt);
        return save(metrics);
    }
}
