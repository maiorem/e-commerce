package com.loopers.batch.writer;

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
        
        // 스코어 기준 정렬 및 상위 N개 선택
        List<ScoredProductMetrics> topMetrics = allMetrics.stream()
                .sorted(Comparator.comparing(m -> m.score().getScore(), Comparator.reverseOrder()))
                .limit(maxRankSize)
                .toList();
        
        if (topMetrics.isEmpty()) {
            return List.of();
        }
        
        // 랭킹 엔티티 생성
        List<WeeklyProductRanking> rankings = IntStream.range(0, topMetrics.size())
                .mapToObj(i -> {
                    ScoredProductMetrics metricWithScore = topMetrics.get(i);
                    return WeeklyProductRanking.create(
                            metricWithScore.metrics().getProductId(),
                            weekYear,
                            weekNumber,
                            i + 1, // 랭킹은 1부터 시작
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
}