package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductOutputInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductV1Dto {

    public record ProductResponseDto(
            Long id,
            String productName,
            String brandName,
            String categoryName,
            int price,
            int likeCount,
            int stockCount
    ) {
        public static ProductResponseDto from(
                Long id,
                String productName,
                String brandName,
                String categoryName,
                int price,
                int likeCount,
                int stockCount
        ) {
            return new ProductResponseDto(id, productName, brandName, categoryName, price, likeCount, stockCount);
        }
    }

    public record ProductListResponse(
            int totalCount,
            int pageSize,
            int pageNumber,
            List<ProductResponseDto> products
    ) {
        public static ProductListResponse from(Page<ProductOutputInfo> productList) {

            List<ProductResponseDto> productResponses = productList.getContent().stream()
                    .map(product -> new ProductResponseDto(
                            product.id(),
                            product.name(),
                            product.brandName(),
                            product.categoryName(),
                            product.price(),
                            product.likeCount(),
                            product.stock()
                    )).toList();

            return new ProductListResponse(
                    (int) productList.getTotalElements(),
                    productList.getSize(),
                    productList.getNumber(),
                    productResponses
            );
        }
    }
}
