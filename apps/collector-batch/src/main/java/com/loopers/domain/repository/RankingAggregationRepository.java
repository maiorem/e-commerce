package com.loopers.domain.repository;

import com.loopers.domain.model.RankedProductScore;

import java.util.Set;

/**
 * 랭킹 집계를 위한 Repository 인터페이스
 */
public interface RankingAggregationRepository {

    /**
     * 주간 랭킹 점수 저장
     */
    void saveWeeklyScore(int weekYear, int weekNumber, String productId, double score);

    /**
     * 월간 랭킹 점수 저장
     */
    void saveMonthlyScore(int year, int month, String productId, double score);

    /**
     * 상위 N개 랭킹 조회
     */
    Set<RankedProductScore> getTopRankings(String rankingKey, int limit);

    /**
     * 임시 데이터 TTL 설정
     */
    void setExpiration(String rankingKey, long timeoutDays);
}
