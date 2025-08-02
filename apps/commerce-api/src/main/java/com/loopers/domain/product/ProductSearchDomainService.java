package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchDomainService {

    /**
     * 상품 검색 조건 유효성 검증
     */
    public void validateSearchCriteria(String productName, int size, int page) {
        if (productName != null && productName.trim().length() < 2) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품명 검색은 2글자 이상 입력해주세요.");
        }
        if (size <= 0 || size > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "페이지 크기는 1~100 사이여야 합니다.");
        }
        if (page < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "페이지 번호는 0 이상이어야 합니다.");
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
            throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 정렬 기준입니다: " + sortBy);
        }
    }

    /**
     * 상품 필터링 조건 검증
     */
    public void validateFilterCriteria(Long brandId, Long categoryId) {
        if (brandId != null && brandId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 양수여야 합니다.");
        }

        if (categoryId != null && categoryId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카테고리 ID는 양수여야 합니다.");
        }
    }

    /**
     * 검색 결과 정렬 적용 (도메인 규칙에 따른 추가 정렬)
     */
    public List<ProductModel> applyDomainSortingRules(List<ProductModel> products, ProductSortBy sortBy) {
        if (sortBy == null) {
            return products; // 기본 정렬 유지
        }
        return switch (sortBy) {
            case LIKES -> products.stream()
                    .sorted((p1, p2) -> {
                        int likesCompare = Integer.compare(p2.getLikesCount(), p1.getLikesCount());
                        if (likesCompare == 0) {
                            // 좋아요 수가 같으면 최신순
                            return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                        }
                        return likesCompare;
                    })
                    .toList();
            case PRICE_ASC -> products.stream()
                    .sorted((p1, p2) -> {
                        int priceCompare = Integer.compare(p1.getPrice(), p2.getPrice());
                        if (priceCompare == 0) {
                            // 가격이 같으면 최신순
                            return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                        }
                        return priceCompare;
                    })
                    .toList();
            case PRICE_DESC -> products.stream()
                    .sorted((p1, p2) -> {
                        int priceCompare = Integer.compare(p2.getPrice(), p1.getPrice());
                        if (priceCompare == 0) {
                            // 가격이 같으면 최신순
                            return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                        }
                        return priceCompare;
                    })
                    .toList();
            case LATEST -> products; 
        };
    }

    /**
     * 검색 결과 필터링 
     */
    public List<ProductModel> applyDomainFilteringRules(List<ProductModel> products) {
        return products.stream()
                .filter(product -> {
                    return product.getStock() > 0;
                })
                .toList();
    }
} 
