package com.loopers.batch.processor;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.ranking.ScoredProductMetrics;
import com.loopers.domain.repository.RankingAggregationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyRankingProcessor implements ItemProcessor<ProductMetrics, ProductMetrics> {

    private final RankingAggregationRepository rankingAggregationRepository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;

    @Override
    public ProductMetrics process(ProductMetrics productMetrics) throws Exception {
        LocalDate targetDate = LocalDate.parse(targetDateParam);
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();

        ScoredProductMetrics scoredMetrics = ScoredProductMetrics.of(productMetrics);
        double score = scoredMetrics.score().getScore();

        rankingAggregationRepository.saveMonthlyScore(
            year, month, productMetrics.getProductId().toString(), score);

        log.debug("월간 랭킹 점수 저장 완료 - Product ID: {}, Score: {}, Month: {}년 {}월",
                productMetrics.getProductId(), score, year, month);

        return null;
    }
}
