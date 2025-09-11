package com.loopers.domain.ranking;

import java.time.LocalDate;

public interface RankingQueryRepository {
    RankingPage getRankingWithProducts(LocalDate date, int page, int size);
}
