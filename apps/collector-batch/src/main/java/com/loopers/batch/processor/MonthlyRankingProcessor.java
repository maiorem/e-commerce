package com.loopers.batch.processor;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.ranking.ScoredProductMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class MonthlyRankingProcessor implements ItemProcessor<ProductMetrics, ScoredProductMetrics> {

    @Override
    public ScoredProductMetrics process(ProductMetrics productMetrics) {
        ScoredProductMetrics result = ScoredProductMetrics.of(productMetrics);

        log.debug("ProductMetrics 처리 완료 - Product ID: {}, Score: {}",
                 result.metrics().getProductId(), result.score().getScore());

        return result;
    }
}