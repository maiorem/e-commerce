package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository {
    
    ProductOptionModel save(ProductOptionModel productOption);
    
    Optional<ProductOptionModel> findById(Long id);
    
    List<ProductOptionModel> findByProductId(Long productId);
    
    void delete(ProductOptionModel productOption);
} 
