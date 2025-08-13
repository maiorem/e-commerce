package com.loopers.domain.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProductSearchDomainServiceTest {

    @InjectMocks
    private ProductSearchDomainService productSearchDomainService;

    private ProductModel product1;
    private ProductModel product2;
    private ProductModel product3;

    @BeforeEach
    void setUp() {
        product1 = ProductModel.builder()
                .brandId(1L)
                .categoryId(1L)
                .name("상품1")
                .description("상품1 설명")
                .price(10000)
                .stock(100)
                .likesCount(10)
                .build();

        product2 = ProductModel.builder()
                .brandId(2L)
                .categoryId(1L)
                .name("상품2")
                .description("상품2 설명")
                .price(20000)
                .stock(50)
                .likesCount(20)
                .build();

        product3 = ProductModel.builder()
                .brandId(3L)
                .categoryId(2L)
                .name("상품3")
                .description("상품3 설명")
                .price(15000)
                .stock(0) // 재고 없음
                .likesCount(5)
                .build();
    }

    @Nested
    @DisplayName("검색 조건 검증 시")
    class Validate_Search_Criteria {

        @Test
        @DisplayName("상품명이 2글자 미만이면 예외가 발생한다.")
        void validateSearchCriteriaShortProductName() {
            // given
            String productName ="a";
            int size = 0;

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(productName, size))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("페이지 사이즈가 음수면 예외가 발생한다.")
        void validateSearchCriteriaZeroOrNegativePageSize() {
            // given
            String productName = "상품";
            int size = -1;

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(productName, size))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("페이지 사이즈가 100보다 크면 예외가 발생한다.")
        void validateSearchCriteriaPageSizeTooLarge() {
            // given
            String productName = "상품";
            int size = 101;

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(productName, size))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("검색 조건 검증 시")
    class Validate_Filter_Criteria {

        @Test
        @DisplayName("브랜드 ID가 잘못 들어가면 예외가 발생한다.")
        void validateFilterCriteriaNegativeBrandId() {
            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateFilterCriteria(-1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("카테고리 ID가 잘못 들어가면 예외가 발생한다.")
        void validateFilterCriteriaNegativeCategoryId() {
            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateFilterCriteria(1L, -1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

} 
