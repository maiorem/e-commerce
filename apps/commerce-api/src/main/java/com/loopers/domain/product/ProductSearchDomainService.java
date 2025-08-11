package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchDomainService {

    /**
     * 상품 검색 조건 유효성 검증
     */
    public void validateSearchCriteria(String productName, int size, int page) {
        if (productName != null && productName.trim().length() < 2) {
            throw new IllegalArgumentException("상품명 검색은 2글자 이상 입력해주세요.");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("페이지 크기는 1~100 사이여야 합니다.");
        }
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0 이상이어야 합니다.");
        }
    }

    /**
     * 상품 정렬 규칙 검증
     */
    public void validateSortCriteria(ProductSortBy sortBy) {
        if (sortBy == null) {
            return; // null은 기본 정렬로 처리
        }

        try {
            ProductSortBy.valueOf(sortBy.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + sortBy);
        }
    }

    /**
     * 상품 필터링 조건 검증
     */
    public void validateFilterCriteria(Long brandId, Long categoryId) {
        if (brandId != null && brandId <= 0) {
            throw new IllegalArgumentException("브랜드 ID는 양수여야 합니다.");
        }

        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException("카테고리 ID는 양수여야 합니다.");
        }
    }
}
