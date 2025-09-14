package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.WeeklyProductRanking;
import com.loopers.domain.repository.WeeklyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyProductRankingRepositoryImpl implements WeeklyProductRankingRepository {

    private final WeeklyProductRankingJpaRepository jpaRepository;

    @Override
    public void saveAllRankings(List<WeeklyProductRanking> rankings) {
        jpaRepository.saveAll(rankings);
    }

    @Override
    public void deleteByWeekYearAndWeekNumber(Integer weekYear, Integer weekNumber) {
        jpaRepository.deleteByWeekYearAndWeekNumber(weekYear, weekNumber);
    }
}
