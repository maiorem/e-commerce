package com.loopers.domain.category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CategoryRepository {
    
    Optional<CategoryModel> findById(Long id);
    
    List<CategoryModel> findAll();

    Optional<CategoryModel> findByName(String categoryName);

    List<CategoryModel> findAllById(Set<Long> categoryIds);
}
