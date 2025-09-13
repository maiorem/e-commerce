package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingItemInfo;
import com.loopers.application.ranking.RankingPageInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class RankingV1Dto {

    @Getter
    @Builder
    public static class RankingPageResponse {
        private final LocalDate date;
        private final int page;
        private final int size;
        private final int totalItems;
        private final List<RankingItemResponse> items;

        public static RankingPageResponse from(RankingPageInfo info) {
            return RankingPageResponse.builder()
                    .date(info.getDate())
                    .page(info.getPage())
                    .size(info.getSize())
                    .totalItems(info.getTotalItems())
                    .items(info.getItems().stream()
                            .map(RankingItemResponse::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class RankingItemResponse {
        private final Long productId;
        private final Long rank;
        private final Double score;
        private final ProductInfoResponse productInfo;

        public static RankingItemResponse from(RankingItemInfo info) {
            return RankingItemResponse.builder()
                    .productId(info.getProductId())
                    .rank(info.getRank())
                    .score(info.getScore())
                    .productInfo(info.getProductInfo() != null ? 
                            ProductInfoResponse.from(info.getProductInfo()) : null)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProductInfoResponse {
        private final String name;
        private final String description;
        private final int price;
        private final String brandName;
        private final long likeCount;

        public static ProductInfoResponse from(RankingItemInfo.ProductInfo info) {
            return ProductInfoResponse.builder()
                    .name(info.getName())
                    .description(info.getDescription())
                    .price(info.getPrice())
                    .brandName(info.getBrandName())
                    .likeCount(info.getLikeCount())
                    .build();
        }
    }
}