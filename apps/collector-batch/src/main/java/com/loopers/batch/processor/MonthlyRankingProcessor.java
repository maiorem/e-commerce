package com.loopers.batch.processor;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.ranking.ScoredProductMetrics;
import com.loopers.infrastructure.redis.RankingRedisService;
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

    private final RankingRedisService rankingRedisService;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;

    @Override
    public ProductMetrics process(ProductMetrics productMetrics) throws Exception {
        // 1. 점수 계산
        ScoredProductMetrics scoredMetrics = ScoredProductMetrics.of(productMetrics);
        double score = scoredMetrics.score().getScore();

        // 2. Redis Key 생성
        LocalDate targetDate = LocalDate.parse(targetDateParam);
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();

        String redisKey = rankingRedisService.getMonthlyRankingKey(year, month);

        // 3. Redis ZSet에 점수 저장
        rankingRedisService.addRankingScore(redisKey, productMetrics.getProductId().toString(), score);

        log.debug("월간 랭킹 점수 Redis 저장 완료 - Product ID: {}, Score: {}, Month: {}년 {}월",
                productMetrics.getProductId(), score, year, month);

        // Writer로 전달하지 않음 (null 반환)
        return null;
    }
}