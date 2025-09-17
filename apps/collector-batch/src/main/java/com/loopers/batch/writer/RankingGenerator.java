package com.loopers.batch.writer;

import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.ranking.ScoredProductMetrics;
import com.loopers.domain.ranking.WeeklyProductRanking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
public class RankingGenerator {

    public List<WeeklyProductRanking> generateWeeklyRankings(
            List<ScoredProductMetrics> allMetrics,
            int weekYear,
            int weekNumber,
            int maxRankSize) {

        List<ScoredProductMetrics> topMetrics = allMetrics.stream()
                .sorted(Comparator.comparing(m -> m.score().getScore(), Comparator.reverseOrder()))
                .limit(maxRankSize)
                .toList();

        if (topMetrics.isEmpty()) {
            return List.of();
        }

        List<WeeklyProductRanking> rankings = IntStream.range(0, topMetrics.size())
                .mapToObj(i -> {
                    ScoredProductMetrics metricWithScore = topMetrics.get(i);
                    return WeeklyProductRanking.create(
                            metricWithScore.metrics().getProductId(),
                            weekYear,
                            weekNumber,
                            i + 1,
                            metricWithScore.score().getScore(),
                            metricWithScore.score().getViewCount(),
                            metricWithScore.score().getLikeCount(),
                            metricWithScore.score().getSalesCount(),
                            metricWithScore.score().getTotalSalesAmount()
                    );
                })
                .toList();

        log.info("주간 랭킹 생성 완료 - {}개 랭킹", rankings.size());
        return rankings;
    }

    public List<MonthlyProductRanking> generateMonthlyRankings(
            List<ScoredProductMetrics> allMetrics,
            int year,
            int month,
            int maxRankSize) {

        List<ScoredProductMetrics> topMetrics = allMetrics.stream()
                .sorted(Comparator.comparing(m -> m.score().getScore(), Comparator.reverseOrder()))
                .limit(maxRankSize)
                .toList();

        if (topMetrics.isEmpty()) {
            return List.of();
        }

        List<MonthlyProductRanking> rankings = IntStream.range(0, topMetrics.size())
                .mapToObj(i -> {
                    ScoredProductMetrics metricWithScore = topMetrics.get(i);
                    return MonthlyProductRanking.create(
                            metricWithScore.metrics().getProductId(),
                            year,
                            month,
                            i + 1,
                            metricWithScore.score().getScore(),
                            metricWithScore.score().getViewCount(),
                            metricWithScore.score().getLikeCount(),
                            metricWithScore.score().getSalesCount(),
                            metricWithScore.score().getTotalSalesAmount()
                    );
                })
                .toList();

        log.info("월간 랭킹 생성 완료 - {}개 랭킹", rankings.size());
        return rankings;
    }
}