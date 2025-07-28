package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    
    Optional<ProductModel> findById(Long id);
    
    List<ProductModel> findByBrandId(Long brandId);
    
    List<ProductModel> findByCategoryId(Long categoryId);
    
    List<ProductModel> findAll();
    
}
