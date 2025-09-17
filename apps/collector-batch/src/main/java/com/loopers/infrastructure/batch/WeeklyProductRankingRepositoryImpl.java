package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.WeeklyProductRanking;
import com.loopers.domain.repository.WeeklyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WeeklyProductRankingRepositoryImpl implements WeeklyProductRankingRepository {

    private final WeeklyProductRankingJpaRepository weeklyProductRankingJpaRepository;

    @Override
    @Transactional
    public void saveAllRankings(List<WeeklyProductRanking> rankings) {
        weeklyProductRankingJpaRepository.saveAll(rankings);
    }

    @Override
    @Transactional
    public void deleteByWeekYearAndWeekNumber(Integer weekYear, Integer weekNumber) {
        weeklyProductRankingJpaRepository.deleteByWeekYearAndWeekNumber(weekYear, weekNumber);
    }

    @Override
    public List<WeeklyProductRanking> findByWeekYearAndWeekNumber(Integer weekYear, Integer weekNumber) {
        return weeklyProductRankingJpaRepository.findByWeekYearAndWeekNumber(weekYear, weekNumber);
    }

    @Override
    public List<WeeklyProductRanking> findTopRankingsByWeekYearAndWeekNumber(
            Integer weekYear, Integer weekNumber, int limit) {
        return weeklyProductRankingJpaRepository.findTopRankingsByWeekYearAndWeekNumber(weekYear, weekNumber, limit);
    }
}