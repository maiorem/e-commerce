package com.loopers.infrastructure.event;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.repository.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.ZoneId.systemDefault;

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
    
    @Override
    public ProductMetrics upsertSalesCount(Long productId, Long amount) {
        ProductMetrics metrics = findByProductId(productId)
                .orElse(ProductMetrics.of(productId));
        
        metrics.incrementSalesCount(amount);
        return save(metrics);
    }
    
    @Override
    public List<ProductMetrics> findAllByLastUpdatedDate(LocalDate date) {
        return jpaRepository.findAllByUpdatedAtBetween(
            date.atStartOfDay().atZone(systemDefault()),
            date.plusDays(1).atStartOfDay().atZone(systemDefault())
        );
    }
}
