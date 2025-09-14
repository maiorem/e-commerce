package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.repository.MonthlyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyProductRankingRepositoryImpl implements MonthlyProductRankingRepository {

    private final MonthlyProductRankingJpaRepository jpaRepository;

    @Override
    public void saveAllRankings(List<MonthlyProductRanking> rankings) {
        jpaRepository.saveAll(rankings);
    }

    @Override
    public void deleteByYearAndMonth(Integer year, Integer month) {
        jpaRepository.deleteByYearAndMonth(year, month);
    }
}