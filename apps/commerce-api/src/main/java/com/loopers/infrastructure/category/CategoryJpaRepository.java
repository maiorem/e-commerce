package com.loopers.infrastructure.category;

import com.loopers.domain.category.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryModel, Long> {
}
