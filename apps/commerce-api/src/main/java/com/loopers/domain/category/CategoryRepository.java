package com.loopers.domain.category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    
    Optional<CategoryModel> findById(Long id);
    
    List<CategoryModel> findAll();
    
}
