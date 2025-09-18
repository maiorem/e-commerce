package com.loopers.infrastructure.batch;

import com.loopers.domain.model.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {
    Optional<ProductMetrics> findByProductId(Long productId);
    Page<ProductMetrics> findByAggregateDateBetween(ZonedDateTime startDate, ZonedDateTime endDate, Pageable pageable);
}
