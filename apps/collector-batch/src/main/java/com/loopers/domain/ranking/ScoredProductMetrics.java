package com.loopers.domain.ranking;

import com.loopers.domain.model.ProductMetrics;

public record ScoredProductMetrics(ProductMetrics metrics, ProductRankingScore score) {

    public static ScoredProductMetrics of(ProductMetrics metrics) {
        ProductRankingScore score = ProductRankingScore.calculate(
                metrics.getViewCount(),
                metrics.getLikeCount(),
                metrics.getSalesCount(),
                metrics.getTotalSalesAmount()
        );
        return new ScoredProductMetrics(metrics, score);
    }
}
