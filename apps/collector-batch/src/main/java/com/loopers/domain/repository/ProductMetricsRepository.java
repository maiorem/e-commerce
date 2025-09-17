package com.loopers.domain.repository;

import com.loopers.domain.entity.ProductMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductMetricsRepository {

    Page<ProductMetrics> findAll(Pageable pageable);

}