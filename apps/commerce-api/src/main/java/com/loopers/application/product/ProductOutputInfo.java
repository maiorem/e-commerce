package com.loopers.application.product;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.product.ProductModel;

import java.time.ZonedDateTime;

@JsonTypeName("ProductOutputInfo")
public record ProductOutputInfo(
        Long id,
        String name,
        String description,
        int price,
        int stock,
        int likeCount,

        Long brandId,
        String brandName,
        String brandDescription,

        Long categoryId,
        String categoryName,
        String categoryDescription,

        ZonedDateTime lastCreatedAt
) {
    public static ProductOutputInfo convertToInfo(
            ProductModel productModel,
            BrandModel brandModel,
            CategoryModel categoryModel
    ) {
        return new ProductOutputInfo(
                productModel.getId(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getStock(),
                productModel.getLikesCount(),
                (brandModel != null) ? brandModel.getId() : null,
                (brandModel != null) ? brandModel.getName() : null,
                (brandModel != null) ? brandModel.getDescription() : null,
                (categoryModel != null) ? categoryModel.getId() : null,
                (categoryModel != null) ? categoryModel.getName() : null,
                (categoryModel != null) ? categoryModel.getDescription() : null,
                productModel.getCreatedAt()
        );
    }
}
