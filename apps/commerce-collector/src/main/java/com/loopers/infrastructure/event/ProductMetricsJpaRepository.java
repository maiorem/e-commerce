package com.loopers.infrastructure.event;

import com.loopers.domain.entity.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Component
public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {
    Optional<ProductMetrics> findByProductId(Long productId);
    
    List<ProductMetrics> findAllByUpdatedAtBetween(ZonedDateTime startDate, ZonedDateTime endDate);
    
    // RepositoryItemReader용 - Pageable 파라미터를 마지막에
    Page<ProductMetrics> findByUpdatedAtBetween(ZonedDateTime startDate, ZonedDateTime endDate, Pageable pageable);
}
