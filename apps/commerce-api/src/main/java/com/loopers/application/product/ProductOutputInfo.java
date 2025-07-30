package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.product.ProductModel;

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
        String categoryDescription
) {
    public static ProductOutputInfo convertToInfo(
            ProductModel productModel,
            BrandModel brandModel,
            CategoryModel categoryModel,
            int likeCount
    ) {
        return new ProductOutputInfo(
                productModel.getId(),
                productModel.getName(),
                productModel.getDescription(),
                productModel.getPrice(),
                productModel.getStock(),
                likeCount,
                (brandModel != null) ? brandModel.getId() : null,
                (brandModel != null) ? brandModel.getName() : null,
                (brandModel != null) ? brandModel.getDescription() : null,
                (categoryModel != null) ? categoryModel.getId() : null,
                (categoryModel != null) ? categoryModel.getName() : null,
                (categoryModel != null) ? categoryModel.getDescription() : null
        );
    }
}
