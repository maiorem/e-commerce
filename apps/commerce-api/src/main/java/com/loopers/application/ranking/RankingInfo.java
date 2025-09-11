package com.loopers.application.ranking;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class RankingInfo {
    private final Long productId;
    private final LocalDate date;
    private final Long rank;
    private final Double score;
}