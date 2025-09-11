package com.loopers.application.event;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.repository.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMetricsRepository productMetricsRepository;
    
    private static final String RANKING_KEY_PREFIX = "ranking:all:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long TTL_DAYS = 2L;
    
    // 이벤트별 가중치
    private static final double VIEW_WEIGHT = 0.1;
    private static final double LIKE_WEIGHT = 0.2;
    private static final double ORDER_WEIGHT = 0.6;

    public String generateDailyKey(LocalDate date) {
        return RANKING_KEY_PREFIX + date.format(DATE_FORMATTER);
    }

    public String generateDailyKey() {
        return generateDailyKey(LocalDate.now());
    }

    public void syncRankingFromDatabase(LocalDate date) {
        log.info("DB에서 Redis로 랭킹 동기화 시작 - Date: {}", date);
        
        String key = generateDailyKey(date);
        
        // 기존 랭킹 데이터 삭제
        redisTemplate.delete(key);
        
        // DB에서 메트릭 데이터 조회
        List<ProductMetrics> metricsList = productMetricsRepository.findAllByLastUpdatedDate(date);
        
        for (ProductMetrics metrics : metricsList) {
            double score = calculateRankingScore(metrics);
            redisTemplate.opsForZSet().add(key, metrics.getProductId().toString(), score);
        }
        
        setTtlIfNew(key);
        
        log.info("DB에서 Redis로 랭킹 동기화 완료 - Date: {}, 처리된 상품 수: {}", date, metricsList.size());
    }
    
    private double calculateRankingScore(ProductMetrics metrics) {
        double viewScore = VIEW_WEIGHT * metrics.getViewCount();
        double likeScore = LIKE_WEIGHT * metrics.getLikeCount();
        double orderScore = ORDER_WEIGHT * Math.log10(metrics.getTotalSalesAmount() + 1);
        
        return viewScore + likeScore + orderScore;
    }

    public Set<Object> getRankingByPage(LocalDate date, int page, int size) {
        String key = generateDailyKey(date);
        int start = (page - 1) * size;
        int end = start + size - 1;
        
        // 내림차순 정렬 (높은 점수부터)
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    public Long getProductRank(Long productId, LocalDate date) {
        String key = generateDailyKey(date);

        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString());
        return rank != null ? rank + 1 : null;
    }

    public Double getProductScore(Long productId, LocalDate date) {
        String key = generateDailyKey(date);
        return redisTemplate.opsForZSet().score(key, productId.toString());
    }

    private void setTtlIfNew(String key) {

        Long ttl = redisTemplate.getExpire(key);
        if (ttl == -1) { // TTL이 설정되지 않은 경우
            redisTemplate.expire(key, java.time.Duration.ofDays(TTL_DAYS));
            log.debug("랭킹 키 TTL 설정 - Key: {}, TTL: {}일", key, TTL_DAYS);
        }
    }
}
