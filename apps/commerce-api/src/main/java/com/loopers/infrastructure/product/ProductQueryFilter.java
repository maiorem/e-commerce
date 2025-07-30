package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSortBy;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

import static com.loopers.domain.product.QProductModel.productModel;


public class ProductQueryFilter {

    public static BooleanBuilder createFilterBuilder(String productName, Long brandId, Long categoryId) {
        BooleanBuilder filter = new BooleanBuilder();

        if (productName != null && !productName.isEmpty()) {
            filter.and(productModel.name.containsIgnoreCase(productName));
        }

        if (brandId != null ) {
            filter.and(productModel.brandId.eq(brandId));
        }

        if (categoryId != null ) {
            filter.and(productModel.categoryId.eq(categoryId));
        }

        return filter;
    }


    public static OrderSpecifier<?> getOrderSpecifier(ProductSortBy sortBy) {
        return sortBy == null ? productModel.createdAt.desc()
                : switch (sortBy) {
                    case ProductSortBy.LATEST -> productModel.createdAt.desc();
                    case ProductSortBy.LIKES -> productModel.likesCount.desc();
                    case ProductSortBy.PRICE_ASC -> productModel.price.asc();
                    case ProductSortBy.PRICE_DESC -> productModel.price.desc();
                    default -> productModel.createdAt.desc();
        };
    }

}
