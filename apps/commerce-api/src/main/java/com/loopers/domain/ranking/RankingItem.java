package com.loopers.domain.ranking;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankingItem {
    private final Long productId;
    private final Long rank;
    private final Double score;
    private final ProductInfo productInfo;
    
    @Getter
    @Builder
    public static class ProductInfo {
        private final String name;
        private final String description;
        private final int price;
        private final String brandName;
        private final long likeCount;
    }
}