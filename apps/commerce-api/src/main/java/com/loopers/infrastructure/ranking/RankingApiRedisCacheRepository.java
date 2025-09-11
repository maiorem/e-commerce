package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingApiCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RankingApiRedisCacheRepository implements RankingApiCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Set<Object> getReversedRank(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    @Override
    public Long getProductRank(String key, Long productId) {
        return redisTemplate.opsForZSet().reverseRank(key, productId.toString());
    }

    @Override
    public Double getProductScore(String key, Long productId) {
        return redisTemplate.opsForZSet().score(key, productId.toString());
    }

    @Override
    public Long getTotalRankingCount(String key) {
        return redisTemplate.opsForZSet().count(key, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
