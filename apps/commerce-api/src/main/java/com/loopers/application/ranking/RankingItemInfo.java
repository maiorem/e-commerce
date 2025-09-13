package com.loopers.application.ranking;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RankingItemInfo {
    private final Long productId;
    private final Long rank;
    private final Double score;
    
    private ProductInfo productInfo;
    
    @Getter
    @Builder
    public static class ProductInfo {
        private final String name;
        private final String description;
        private final int price;
        private final String brandName;
        private final long likeCount;
    }
    
    public RankingItemInfo withProductInfo(ProductInfo productInfo) {
        this.productInfo = productInfo;
        return this;
    }
}
