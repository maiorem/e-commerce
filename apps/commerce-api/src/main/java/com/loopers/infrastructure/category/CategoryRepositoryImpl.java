package com.loopers.infrastructure.category;

import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryJpaRepository categoryJpaRepository;


    @Override
    public Optional<CategoryModel> findById(Long id) {
        return categoryJpaRepository.findById(id);
    }

    @Override
    public List<CategoryModel> findAll() {
        return categoryJpaRepository.findAll();
    }

    @Override
    public Optional<CategoryModel> findByName(String categoryName) {
        return categoryJpaRepository.findByName(categoryName);
    }

}
