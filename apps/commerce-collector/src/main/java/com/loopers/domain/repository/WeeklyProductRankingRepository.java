package com.loopers.domain.repository;

import com.loopers.domain.ranking.WeeklyProductRanking;

import java.util.List;

public interface WeeklyProductRankingRepository {
    void saveAllRankings(List<WeeklyProductRanking> rankings);
    void deleteByWeekYearAndWeekNumber(Integer weekYear, Integer weekNumber);
}
