package com.loopers.infrastructure.redis;

import com.loopers.domain.repository.RankingAggregationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RankingRedisRepository implements RankingAggregationRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveWeeklyScore(int weekYear, int weekNumber, String productId, double score) {
        String redisKey = getWeeklyRankingKey(weekYear, weekNumber);
        redisTemplate.opsForZSet().add(redisKey, productId, score);
        log.debug("주간 랭킹 점수 저장 - Key: {}, Product: {}, Score: {}", redisKey, productId, score);
    }

    @Override
    public void saveMonthlyScore(int year, int month, String productId, double score) {
        String redisKey = getMonthlyRankingKey(year, month);
        redisTemplate.opsForZSet().add(redisKey, productId, score);
        log.debug("월간 랭킹 점수 저장 - Key: {}, Product: {}, Score: {}", redisKey, productId, score);
    }

    @Override
    public Set<RankedProductScore> getTopRankings(String rankingKey, int limit) {
        Set<ZSetOperations.TypedTuple<String>> rankings = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, 0, limit - 1);

        if (rankings == null) {
            log.warn("Redis에서 랭킹 데이터를 찾을 수 없습니다 - Key: {}", rankingKey);
            return Set.of();
        }

        log.info("Redis에서 상위 {} 랭킹 조회 완료 - Key: {}, 조회된 수: {}",
                limit, rankingKey, rankings.size());

        return rankings.stream()
                .map(tuple -> new RankedProductScore(tuple.getValue(), tuple.getScore()))
                .collect(Collectors.toSet());
    }

    @Override
    public void setExpiration(String rankingKey, long timeoutDays) {
        redisTemplate.expire(rankingKey, timeoutDays, TimeUnit.DAYS);
        log.info("Redis 랭킹 데이터 TTL 설정 - Key: {}, TTL: {}일", rankingKey, timeoutDays);
    }

    private String getWeeklyRankingKey(int weekYear, int weekNumber) {
        return String.format("ranking:weekly:%d:%d", weekYear, weekNumber);
    }

    private String getMonthlyRankingKey(int year, int month) {
        return String.format("ranking:monthly:%d:%02d", year, month);
    }
}