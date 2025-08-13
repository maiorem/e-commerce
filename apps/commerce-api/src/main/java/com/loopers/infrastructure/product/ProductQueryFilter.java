package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSortBy;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;

import static com.loopers.domain.product.QProductModel.productModel;


public class ProductQueryFilter {

    public static BooleanBuilder createFilterBuilder(
            String productName,
            Long brandId,
            Long categoryId,
            ProductSortBy sortBy,
            // 커서 기반 페이지네이션을 위한 추가 파라미터
            CursorFilter cursorFilter

    ) {
        BooleanBuilder filter = new BooleanBuilder();

        if (productName != null && !productName.isEmpty()) {
            filter.and(productModel.name.containsIgnoreCase(productName));
        }

        if (brandId != null) {
            filter.and(productModel.brandId.eq(brandId));
        }

        if (categoryId != null) {
            filter.and(productModel.categoryId.eq(categoryId));
        }

        // --- 커서 기반 페이지네이션 조건 ---
        if (cursorFilter.lastId() != null) {
            // 정렬 기준이 없거나 LIKES일 경우 (기본 정렬)
            if (sortBy == null || sortBy == ProductSortBy.LIKES) {
                filter.and(productModel.likesCount.lt(cursorFilter.lastLikesCount())
                        .or(productModel.likesCount.eq(cursorFilter.lastLikesCount()).and(productModel.id.lt(cursorFilter.lastId()))));
            }
            // LATEST 정렬
            else if (sortBy == ProductSortBy.LATEST) {
                filter.and(productModel.createdAt.lt(cursorFilter.lastCreatedAt())
                        .or(productModel.createdAt.eq(cursorFilter.lastCreatedAt()).and(productModel.id.lt(cursorFilter.lastId()))));
            }
            // PRICE_ASC 정렬
            else if (sortBy == ProductSortBy.PRICE_ASC) {
                filter.and(productModel.price.gt(cursorFilter.lastPrice())
                        .or(productModel.price.eq(cursorFilter.lastPrice()).and(productModel.id.lt(cursorFilter.lastId()))));
            }
            // PRICE_DESC 정렬
            else if (sortBy == ProductSortBy.PRICE_DESC) {
                filter.and(productModel.price.lt(cursorFilter.lastPrice())
                        .or(productModel.price.eq(cursorFilter.lastPrice()).and(productModel.id.lt(cursorFilter.lastId()))));
            }
        }
        return filter;
    }


    public static OrderSpecifier<?>[] getOrderSpecifier(ProductSortBy sortBy) {
        OrderSpecifier<?> primaryOrder;
        OrderSpecifier<?> secondaryOrder = productModel.id.desc(); // 보조 정렬 기준

        if (sortBy == null || sortBy == ProductSortBy.LIKES) {
            primaryOrder = productModel.likesCount.desc();
        } else if (sortBy == ProductSortBy.LATEST) {
            primaryOrder = productModel.createdAt.desc();
        } else if (sortBy == ProductSortBy.PRICE_ASC) {
            primaryOrder = productModel.price.asc();
            secondaryOrder = productModel.id.asc(); // 가격 오름차순일 때 ID도 오름차순
        } else if (sortBy == ProductSortBy.PRICE_DESC) {
            primaryOrder = productModel.price.desc();
        } else {
            primaryOrder = productModel.likesCount.desc();
        }

        // 1차 정렬 기준과 2차 정렬 기준을 함께 사용
        return new OrderSpecifier<?>[] { primaryOrder, secondaryOrder };
    }
}
