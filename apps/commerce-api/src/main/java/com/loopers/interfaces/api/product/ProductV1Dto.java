package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductOutputInfo;
import com.loopers.domain.product.ProductSortBy;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.ZonedDateTime;
import java.util.List;

public class ProductV1Dto {

    public record ProductListRequest(
            @Parameter(description = "상품명 검색 키워드")
            String productName,
            @Parameter(description = "브랜드 ID")
            Long brandId,
            @Parameter(description = "카테고리 ID")
            Long categoryId,
            @Parameter(description = "정렬 기준 (LIKES, LATEST, PRICE_ASC, PRICE_DESC)", example = "LIKES")
            ProductSortBy sortBy,
            @Parameter(description = "페이지 크기", example = "20")
            int pageSize,
            @Parameter(description = "마지막 상품 ID (커서)")
            Long lastId,
            @Parameter(description = "마지막 상품 좋아요 수 (커서)")
            Integer lastLikesCount,
            @Parameter(description = "마지막 상품 가격 (커서)")
            Integer lastPrice,
            @Parameter(description = "마지막 상품 생성일시 (커서)")
            String lastCreatedAt
    ) {}

    public record ProductResponseDto(
            Long id,
            String productName,
            String brandName,
            String categoryName,
            int price,
            int likeCount,
            int stockCount,
            RankingInfoDto rankingInfo
    ) {
        public static ProductResponseDto from(
                Long id,
                String productName,
                String brandName,
                String categoryName,
                int price,
                int likeCount,
                int stockCount,
                RankingInfoDto rankingInfo
        ) {
            return new ProductResponseDto(id, productName, brandName, categoryName, price, likeCount, stockCount, rankingInfo);
        }

        public static ProductResponseDto from(
                Long id,
                String productName,
                String brandName,
                String categoryName,
                int price,
                int likeCount,
                int stockCount
        ) {
            return new ProductResponseDto(id, productName, brandName, categoryName, price, likeCount, stockCount, null);
        }
    }

    public record ProductListResponse(
            int pageSize,
            List<ProductResponseDto> products,
            // 다음 페이지 조회를 위한 커서 정보
            CursorInfo nextCursor
    ) {
        public static ProductListResponse from(List<ProductOutputInfo> productList, int pageSize) {
            List<ProductResponseDto> productResponses = productList.stream()
                    .map(product -> new ProductResponseDto(
                            product.id(),
                            product.name(),
                            product.brandName(),
                            product.categoryName(),
                            product.price(),
                            product.likeCount(),
                            product.stock(),
                            null // 목록에서는 랭킹 정보 제외
                    )).toList();

            // 마지막 상품의 정보로 다음 커서 생성
            CursorInfo nextCursor = productList.isEmpty() ? null : 
                CursorInfo.from(productList.get(productList.size() - 1));

            return new ProductListResponse(pageSize, productResponses, nextCursor);
        }
    }

    public record CursorInfo(
            Long lastId,
            Integer lastLikesCount,
            Integer lastPrice,
            ZonedDateTime lastCreatedAt
    ) {
        public static CursorInfo from(ProductOutputInfo lastProduct) {
            return new CursorInfo(
                    lastProduct.id(),
                    lastProduct.likeCount(),
                    lastProduct.price(),
                    lastProduct.lastCreatedAt()
            );
        }
    }

    public record RankingInfoDto(
            Long rank,
            Double score
    ) {
        public static RankingInfoDto from(Long rank, Double score) {
            return new RankingInfoDto(rank, score);
        }
    }
}
