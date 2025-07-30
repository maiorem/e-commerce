package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    
    Optional<ProductModel> findById(Long id);
    
    List<ProductModel> findByBrandId(Long brandId);
    
    List<ProductModel> findByCategoryId(Long categoryId);

    Page<ProductModel> findSearchProductList(Pageable pageable, String productName, Long brandId, Long categoryId, ProductSortBy sortBy);
    
    void save(ProductModel product);

    List<ProductModel> findAllByIds(List<Long> productIds);
}
