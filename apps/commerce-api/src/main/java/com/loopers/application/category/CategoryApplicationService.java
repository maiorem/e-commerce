package com.loopers.application.category;

import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * 카테고리 목록 조회 (캐싱 적용)
     */
    @Cacheable(value = "category", key = "'list'")
    public List<CategoryModel> getCategoryList() {
        log.info("카테고리 목록 DB 조회 (캐시 미스)");
        return categoryRepository.findAll();
    }

    /**
     * 카테고리 상세 조회 (캐싱 적용)
     */
    @Cacheable(value = "category", key = "#categoryId")
    public CategoryModel getCategoryDetail(Long categoryId) {
        log.info("카테고리 상세 DB 조회 (캐시 미스) - 카테고리ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "카테고리 정보를 찾을 수 없습니다."));
    }

    /**
     * 카테고리별 상품 목록 조회
     */
    public List<ProductModel> getProductList(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
} 
