package com.loopers.batch.processor;

import com.loopers.domain.model.ProductMetrics;
import com.loopers.domain.ranking.ScoredProductMetrics;
import com.loopers.domain.repository.RankingAggregationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class WeeklyRankingProcessor implements ItemProcessor<ProductMetrics, ProductMetrics> {

    private final RankingAggregationRepository rankingAggregationRepository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;

    @Override
    public ProductMetrics process(ProductMetrics productMetrics) throws Exception {
        LocalDate targetDate = LocalDate.parse(targetDateParam);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekYear = targetDate.get(weekFields.weekBasedYear());
        int weekNumber = targetDate.get(weekFields.weekOfWeekBasedYear());

        ScoredProductMetrics scoredMetrics = ScoredProductMetrics.of(productMetrics);
        double score = scoredMetrics.score().getScore();

        rankingAggregationRepository.saveWeeklyScore(
            weekYear, weekNumber, productMetrics.getProductId().toString(), score);

        log.debug("주간 랭킹 점수 저장 완료 - Product ID: {}, Score: {}, Week: {}년 {}주차",
                productMetrics.getProductId(), score, weekYear, weekNumber);

        // Writer로 전달하지 않음
        return null;
    }
}
