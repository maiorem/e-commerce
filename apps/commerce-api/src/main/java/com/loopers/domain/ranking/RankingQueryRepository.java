package com.loopers.domain.ranking;

import java.time.LocalDate;

public interface RankingQueryRepository {
    RankingPage getRankingWithProducts(LocalDate date, int page, int size);
    RankingPage getWeeklyRankingWithProducts(LocalDate date, int page, int size);
    RankingPage getMonthlyRankingWithProducts(LocalDate date, int page, int size);
}
