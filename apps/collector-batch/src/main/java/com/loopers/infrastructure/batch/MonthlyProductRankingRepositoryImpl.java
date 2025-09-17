package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.repository.MonthlyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MonthlyProductRankingRepositoryImpl implements MonthlyProductRankingRepository {

    private final MonthlyProductRankingJpaRepository monthlyProductRankingJpaRepository;

    @Override
    @Transactional
    public void saveAllRankings(List<MonthlyProductRanking> rankings) {
        monthlyProductRankingJpaRepository.saveAll(rankings);
    }

    @Override
    @Transactional
    public void deleteByYearAndMonth(Integer year, Integer month) {
        monthlyProductRankingJpaRepository.deleteByYearAndMonth(year, month);
    }

    @Override
    public List<MonthlyProductRanking> findByYearAndMonth(Integer year, Integer month) {
        return monthlyProductRankingJpaRepository.findByYearAndMonth(year, month);
    }

    @Override
    public List<MonthlyProductRanking> findTopRankingsByYearAndMonth(
            Integer year, Integer month, int limit) {
        return monthlyProductRankingJpaRepository.findTopRankingsByYearAndMonth(year, month, limit);
    }
}