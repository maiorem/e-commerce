package com.loopers.batch.tasklet;

import com.loopers.domain.model.RankedProductScore;
import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.repository.MonthlyProductRankingRepository;
import com.loopers.domain.repository.RankingAggregationRepository;
import com.loopers.domain.repository.ProductMetricsRepository;
import com.loopers.domain.model.ProductMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyRankingPersistenceTasklet implements Tasklet {

    private final RankingAggregationRepository rankingAggregationRepository;
    private final MonthlyProductRankingRepository monthlyProductRankingRepository;
    private final ProductMetricsRepository productMetricsRepository;

    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;

    @Value("#{jobParameters['maxRankSize'] ?: 100}")
    private int maxRankSize;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LocalDate targetDate = LocalDate.parse(targetDateParam);
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();

        String rankingKey = String.format("ranking:monthly:%d:%02d", year, month);

        log.info("월간 랭킹 영속화 시작 - {}년 {}월", year, month);

        // 1. 기존 랭킹 데이터 삭제
        monthlyProductRankingRepository.deleteByYearAndMonth(year, month);

        // 2. Repository에서 TOP N 랭킹 조회
        Set<RankedProductScore> topRankings =
            rankingAggregationRepository.getTopRankings(rankingKey, maxRankSize);

        if (topRankings.isEmpty()) {
            log.warn("조회된 랭킹 데이터가 없습니다 - Key: {}", rankingKey);
            return RepeatStatus.FINISHED;
        }

        List<MonthlyProductRanking> rankings = new ArrayList<>();
        int rank = 1;

        for (RankedProductScore rankedScore : topRankings) {
            if (rankedScore.productId() != null && rankedScore.score() != null) {
                Long productId = Long.parseLong(rankedScore.productId());

                ProductMetrics metrics = productMetricsRepository.findByProductId(productId)
                    .orElse(null);

                if (metrics != null) {
                    MonthlyProductRanking ranking = MonthlyProductRanking.create(
                        productId,
                        year,
                        month,
                        rank,
                        rankedScore.score(),
                        metrics.getViewCount(),
                        metrics.getLikeCount(),
                        metrics.getSalesCount(),
                        metrics.getTotalSalesAmount()
                    );
                    rankings.add(ranking);
                    rank++;
                } else {
                    log.warn("ProductMetrics 조회 실패 - Product ID: {}", productId);
                }
            }
        }

        // 3. DB에 영속화
        monthlyProductRankingRepository.saveAllRankings(rankings);

        // 4. Redis 임시 데이터 정리 (30일 후 만료)
        rankingAggregationRepository.setExpiration(rankingKey, 30);

        log.info("월간 랭킹 영속화 완료 - {}년 {}월, 저장된 랭킹 수: {}개",
                year, month, rankings.size());

        contribution.setExitStatus(ExitStatus.COMPLETED);
        return RepeatStatus.FINISHED;
    }
}
