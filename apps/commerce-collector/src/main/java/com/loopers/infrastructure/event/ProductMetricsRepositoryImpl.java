package com.loopers.infrastructure.event;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.repository.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
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
    public void incrementViewCountBatch(Long productId, int incrementCount, ZonedDateTime lastViewedAt) {
        ProductMetrics metrics = findByProductId(productId)
                .orElse(ProductMetrics.of(productId));
        
        // 기존 조회수에 증가분 추가
        for (int i = 0; i < incrementCount; i++) {
            metrics.incrementViewCount(lastViewedAt);
        }
        save(metrics);
    }
    
    @Override
    public void updateLikeCountBatch(Long productId, Long finalCount, ZonedDateTime lastLikedAt) {
        ProductMetrics metrics = findByProductId(productId)
                .orElse(ProductMetrics.of(productId));
        
        metrics.updateLikeCount(finalCount, lastLikedAt);
        save(metrics);
    }
    
    @Override
    public void incrementSalesCountBatch(Long productId, int salesCount, Long totalAmount) {
        ProductMetrics metrics = findByProductId(productId)
                .orElse(ProductMetrics.of(productId));
        
        // 판매 건수와 금액을 증가분만큼 추가
        for (int i = 0; i < salesCount; i++) {
            long avgAmount = totalAmount / salesCount;
            metrics.incrementSalesCount(avgAmount);
        }
        save(metrics);
    }

    @Override
    public List<ProductMetrics> findAll() {
        return jpaRepository.findAll();
    }
}
