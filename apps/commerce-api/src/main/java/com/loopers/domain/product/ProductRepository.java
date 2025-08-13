package com.loopers.domain.product;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    
    Optional<ProductModel> findById(Long id);

    Optional<ProductModel> findByIdForUpdate(Long id);
    
    List<ProductModel> findByBrandId(Long brandId);
    
    List<ProductModel> findByCategoryId(Long categoryId);

    List<ProductModel> findSearchProductList(int size, String productName, Long brandId, Long categoryId,
    ProductSortBy sortBy, Long lastId, Integer lastLikesCount, Integer lastPrice, ZonedDateTime lastCreatedAt);
    
    ProductModel save(ProductModel product);

    List<ProductModel> findAllByIds(List<Long> productIds);
}
