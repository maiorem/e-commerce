package com.loopers.application.product;

import com.loopers.domain.product.ProductSortBy;
import lombok.Getter;

@Getter
public class ProductQuery {

    private String productName;
    private String brandName;
    private String categoryName;
    private ProductSortBy sortBy;
    private int page;
    private int size;

    public static ProductQuery from(String productName, String brandName, String categoryName, ProductSortBy sortBy, int page, int size) {
        ProductQuery info = new ProductQuery();
        info.productName = productName;
        info.brandName = brandName;
        info.categoryName = categoryName;
        info.sortBy = sortBy;
        info.page = page;
        info.size = size;
        return info;
    }

}
