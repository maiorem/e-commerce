package com.loopers.infrastructure.batch;

import com.loopers.domain.entity.ProductMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {

    Page<ProductMetrics> findByUpdatedAtBetween(ZonedDateTime start, ZonedDateTime end, Pageable pageable);

}
