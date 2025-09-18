package com.loopers.domain.repository;

import com.loopers.domain.model.ProductMetrics;

import java.util.Optional;

public interface ProductMetricsRepository {

    Optional<ProductMetrics> findByProductId(Long productId);
}
