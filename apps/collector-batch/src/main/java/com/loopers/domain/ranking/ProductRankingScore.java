package com.loopers.domain.ranking;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductRankingScore {

    private final Long viewCount;
    private final Long likeCount;
    private final Long salesCount;
    private final Long totalSalesAmount;
    private final Double score;

    public static ProductRankingScore calculate(Long viewCount, Long likeCount, Long salesCount, Long totalSalesAmount) {
        double calculatedScore = viewCount + (likeCount * 2) + (salesCount * 5) + (totalSalesAmount * 0.001);

        return new ProductRankingScore(viewCount, likeCount, salesCount, totalSalesAmount, calculatedScore);
    }
}