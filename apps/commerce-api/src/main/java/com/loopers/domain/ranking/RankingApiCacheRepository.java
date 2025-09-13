package com.loopers.domain.ranking;

import java.util.Set;

public interface RankingApiCacheRepository {
    Set<Object> getReversedRank(String key, long start, long end);
    Long getProductRank(String key, Long productId);
    Double getProductScore(String key, Long productId);
    Long getTotalRankingCount(String key);
}
