package com.loopers.infrastructure.redis;

import com.loopers.domain.repository.RankingCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RankingRedisCacheRepository implements RankingCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addOrUpdateScore(String key, String member, double score) {
        redisTemplate.opsForZSet().add(key, member, score);
    }

    @Override
    public void incrementScore(String key, String member, double score) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    @Override
    public void unionAndStore(String sourceKey, String destinationKey) {
        redisTemplate.opsForZSet().unionAndStore(sourceKey, destinationKey, destinationKey);
    }

    @Override
    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }
}
