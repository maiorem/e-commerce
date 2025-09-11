package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RANKING_KEY_PREFIX = "ranking:all:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateDailyKey(LocalDate date) {
        return RANKING_KEY_PREFIX + date.format(DATE_FORMATTER);
    }

    public String generateDailyKey() {
        return generateDailyKey(LocalDate.now());
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

        // reverseRank (높은 점수부터 0번째 순위)
        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString());
        return rank != null ? rank + 1 : null; // 1부터 시작하는 순위로 변환
    }

    public Double getProductScore(Long productId, LocalDate date) {
        String key = generateDailyKey(date);
        return redisTemplate.opsForZSet().score(key, productId.toString());
    }

    public Long getTotalRankingCount(LocalDate date) {
        String key = generateDailyKey(date);
        return redisTemplate.opsForZSet().count(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
