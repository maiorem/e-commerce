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
        double viewScore = 0.1 * viewCount;
        double likeScore = 0.2 * likeCount;
        double orderScore = 0.6 * Math.log10(totalSalesAmount + 1);
        double calculatedScore = viewScore + likeScore + orderScore;

        return new ProductRankingScore(viewCount, likeCount, salesCount, totalSalesAmount, calculatedScore);
    }
}
