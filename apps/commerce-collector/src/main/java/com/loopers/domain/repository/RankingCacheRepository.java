package com.loopers.domain.repository;

import java.time.Duration;

public interface RankingCacheRepository {
    void addOrUpdateScore(String key, String member, double score);
    void incrementScore(String key, String member, double score);
    void unionAndStore(String sourceKey, String destinationKey);
    void expire(String key, Duration ttl);
}
