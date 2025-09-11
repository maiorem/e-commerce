package com.loopers.application.event;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.repository.ProductMetricsRepository;
import com.loopers.domain.repository.RankingCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private final ProductMetricsRepository productMetricsRepository;
    private final RankingCacheRepository cacheRepository;

    private static final String RANKING_KEY_PREFIX = "ranking:all:";
    private static final Duration RANKING_TTL = Duration.ofDays(2);

    // 이벤트별 가중치
    private static final double VIEW_WEIGHT = 0.1;
    private static final double LIKE_WEIGHT = 0.2;
    private static final double ORDER_WEIGHT = 0.6;

    public String generateDailyKey(LocalDate date) {
        return RANKING_KEY_PREFIX + date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * Redis 랭킹을 업데이트
     */
    public void updateDailyRanking(LocalDate date) {
        String key = generateDailyKey(date);
        List<ProductMetrics> allMetrics = productMetricsRepository.findAll(); // DB에서 모든 상품 메트릭 조회

        for (ProductMetrics metric : allMetrics) {
            double score = calculateRankingScore(metric);
            if (score > 0) {
                cacheRepository.addOrUpdateScore(key, metric.getProductId().toString(), score);
            }
        }

        cacheRepository.expire(key, RANKING_TTL);
        log.info("일간 랭킹 업데이트 완료 - 키: {}", key);
    }

    /**
     * 전날의 랭킹을 다음날의 랭킹으로 이월.
     */
    public void carryOverRankingScores() {
        String todayKey = generateDailyKey(LocalDate.now());
        String tomorrowKey = generateDailyKey(LocalDate.now().plusDays(1));

        cacheRepository.unionAndStore(todayKey, tomorrowKey);
        cacheRepository.expire(tomorrowKey, RANKING_TTL);
        log.info("랭킹 점수 이월 완료: {} -> {}", todayKey, tomorrowKey);
    }

    private double calculateRankingScore(ProductMetrics metrics) {
        double viewScore = VIEW_WEIGHT * metrics.getViewCount();
        double likeScore = LIKE_WEIGHT * metrics.getLikeCount();
        double orderScore = ORDER_WEIGHT * Math.log10(metrics.getTotalSalesAmount() + 1);
        return viewScore + likeScore + orderScore;
    }

}
