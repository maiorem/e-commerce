package com.loopers.infrastructure.category;

import com.loopers.domain.category.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryJpaRepository extends JpaRepository<CategoryModel, Long> {
    Optional<CategoryModel> findByName(String categoryName);
}
