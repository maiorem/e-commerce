package com.loopers.domain.repository;

import com.loopers.domain.ranking.MonthlyProductRanking;

import java.util.List;

public interface MonthlyProductRankingRepository {
    void saveAllRankings(List<MonthlyProductRanking> rankings);
    void deleteByYearAndMonth(Integer year, Integer month);
}