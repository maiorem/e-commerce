package com.loopers.application.category;

import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryModel> getCategoryList() {
        return  categoryRepository.findAll();
    }

    public CategoryModel getCategoryDetail(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "카테고리 정보를 찾을 수 없습니다."));
    }

    public List<ProductModel> getProductList(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
} 
