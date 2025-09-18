package com.loopers.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 주간 랭킹 Redis Key 생성
     */
    public String getWeeklyRankingKey(int weekYear, int weekNumber) {
        return String.format("ranking:weekly:%d:%d", weekYear, weekNumber);
    }

    /**
     * 월간 랭킹 Redis Key 생성
     */
    public String getMonthlyRankingKey(int year, int month) {
        return String.format("ranking:monthly:%d:%02d", year, month);
    }

    /**
     * 상품의 랭킹 점수를 Redis ZSet에 추가/업데이트
     */
    public void addRankingScore(String redisKey, String productId, double score) {
        redisTemplate.opsForZSet().add(redisKey, productId, score);
        log.debug("Redis ZSet에 점수 추가 - Key: {}, Product: {}, Score: {}", redisKey, productId, score);
    }

    /**
     * 상위 N개 랭킹 조회 (점수 높은 순)
     */
    public Set<ZSetOperations.TypedTuple<String>> getTopRankings(String redisKey, int limit) {
        Set<ZSetOperations.TypedTuple<String>> rankings = redisTemplate.opsForZSet()
                .reverseRangeWithScores(redisKey, 0, limit - 1);

        log.info("Redis에서 상위 {} 랭킹 조회 완료 - Key: {}, 조회된 수: {}",
                limit, redisKey, rankings != null ? rankings.size() : 0);

        return rankings;
    }

    /**
     * 랭킹 데이터 초기화
     */
    public void clearRanking(String redisKey) {
        redisTemplate.delete(redisKey);
        log.info("Redis 랭킹 데이터 초기화 - Key: {}", redisKey);
    }

    /**
     * 랭킹 데이터에 TTL 설정 (선택적)
     */
    public void setRankingExpire(String redisKey, long timeout, TimeUnit unit) {
        redisTemplate.expire(redisKey, timeout, unit);
        log.info("Redis 랭킹 데이터 TTL 설정 - Key: {}, TTL: {} {}", redisKey, timeout, unit);
    }

    /**
     * 현재 랭킹 데이터 크기 조회
     */
    public Long getRankingSize(String redisKey) {
        return redisTemplate.opsForZSet().size(redisKey);
    }
}