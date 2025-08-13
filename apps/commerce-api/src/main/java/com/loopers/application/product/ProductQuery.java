package com.loopers.application.product;

import com.loopers.domain.product.ProductSortBy;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class ProductQuery {

    private String productName;
    private Long brandId;
    private Long categoryId;
    private ProductSortBy sortBy;
    private int size;
    private Long lastId;
    private Integer lastLikesCount;
    private Integer lastPrice;
    private ZonedDateTime lastCreatedAt;

    public static ProductQuery from(String productName, Long brandId, Long categoryId, ProductSortBy sortBy, int size,
                                    Long lastId, Integer lastLikesCount, Integer lastPrice, String lastCreatedAt) {
        ProductQuery info = new ProductQuery();
        info.productName = productName;
        info.brandId = brandId;
        info.categoryId = categoryId;
        info.sortBy = sortBy;
        info.size = size;
        info.lastId = lastId;
        info.lastLikesCount = lastLikesCount;
        info.lastPrice = lastPrice;
        info.lastCreatedAt = lastCreatedAt != null ? ZonedDateTime.parse(lastCreatedAt) : null;
        return info;
    }

}
