package com.loopers.domain.product;

import com.loopers.application.product.ProductQuery;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        @DisplayName("정상적인 검색 조건은 검증을 통과한다.")
        void validateSearchCriteriaSuccess() {
            // given
            ProductQuery query = ProductQuery.from("상품", "브랜드", "카테고리", ProductSortBy.LATEST, 0, 10);

            // when & then
            productSearchDomainService.validateSearchCriteria(query);
            // 예외가 발생하지 않으면 성공
        }

        @Test
        @DisplayName("상품명이 2글자 미만이면 예외가 발생한다.")
        void validateSearchCriteriaShortProductName() {
            // given
            ProductQuery query = ProductQuery.from("a", "브랜드", "카테고리", ProductSortBy.LATEST, 0, 10);

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(query))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("페이지 크기가 0 이하면 예외가 발생한다.")
        void validateSearchCriteriaInvalidPageSize() {
            // given
            ProductQuery query = ProductQuery.from("상품", "브랜드", "카테고리", ProductSortBy.LATEST, 0, 0);

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(query))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("페이지 크기가 100을 초과하면 예외가 발생한다.")
        void validateSearchCriteriaTooLargePageSize() {
            // given
            ProductQuery query = ProductQuery.from("상품", "브랜드", "카테고리", ProductSortBy.LATEST, 0, 101);

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(query))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("페이지 번호가 음수이면 예외가 발생한다.")
        void validateSearchCriteriaNegativePage() {
            // given
            ProductQuery query = ProductQuery.from("상품", "브랜드", "카테고리", ProductSortBy.LATEST, -1, 10);

            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateSearchCriteria(query))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
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
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("카테고리 ID가 잘못 들어가면 예외가 발생한다.")
        void validateFilterCriteriaNegativeCategoryId() {
            // when & then
            assertThatThrownBy(() -> productSearchDomainService.validateFilterCriteria(1L, -1L))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", com.loopers.support.error.ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("정렬 규칙 적용 시")
    class Apply_Domain_Sorting_Rules {

        @Test
        @DisplayName("좋아요순 정렬을 적용할 수 있다.")
        void applyDomainSortingRulesLikes() {
            // given
            List<ProductModel> products = List.of(product1, product2, product3);

            // when
            List<ProductModel> result = productSearchDomainService.applyDomainSortingRules(products, ProductSortBy.LIKES);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getLikesCount()).isEqualTo(20); // product2
            assertThat(result.get(1).getLikesCount()).isEqualTo(10); // product1
            assertThat(result.get(2).getLikesCount()).isEqualTo(5);  // product3
        }

        @Test
        @DisplayName("가격 오름차순 정렬을 적용할 수 있다.")
        void applyDomainSortingRulesPriceAsc() {
            // given
            List<ProductModel> products = List.of(product2, product1, product3);

            // when
            List<ProductModel> result = productSearchDomainService.applyDomainSortingRules(products, ProductSortBy.PRICE_ASC);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getPrice()).isEqualTo(10000); // product1
            assertThat(result.get(1).getPrice()).isEqualTo(15000); // product3
            assertThat(result.get(2).getPrice()).isEqualTo(20000); // product2
        }

        @Test
        @DisplayName("가격 내림차순 정렬을 적용할 수 있다.")
        void applyDomainSortingRulesPriceDesc() {
            // given
            List<ProductModel> products = List.of(product1, product2, product3);

            // when
            List<ProductModel> result = productSearchDomainService.applyDomainSortingRules(products, ProductSortBy.PRICE_DESC);

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getPrice()).isEqualTo(20000); // product2
            assertThat(result.get(1).getPrice()).isEqualTo(15000); // product3
            assertThat(result.get(2).getPrice()).isEqualTo(10000); // product1
        }

        @Test
        @DisplayName("null 정렬 기준은 원본 순서를 유지한다.")
        void applyDomainSortingRulesNull() {
            // given
            List<ProductModel> products = List.of(product1, product2, product3);

            // when
            List<ProductModel> result = productSearchDomainService.applyDomainSortingRules(products, null);

            // then
            assertThat(result).isEqualTo(products);
        }
    }

    @Nested
    @DisplayName("도메인 필터링 규칙 적용 시")
    class Apply_Domain_Filtering_Rules {

        @Test
        @DisplayName("재고가 0인 상품을 필터링한다.")
        void applyDomainFilteringRules() {
            // given
            List<ProductModel> products = List.of(product1, product2, product3);

            // when
            List<ProductModel> result = productSearchDomainService.applyDomainFilteringRules(products);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(product1, product2);
            assertThat(result).doesNotContain(product3); // 재고가 0인 상품 제외
        }

        @Test
        @DisplayName("모든 상품에 재고가 있으면 필터링하지 않는다.")
        void applyDomainFilteringRulesAllInStock() {
            // given
            List<ProductModel> products = List.of(product1, product2);

            // when
            List<ProductModel> result = productSearchDomainService.applyDomainFilteringRules(products);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).contains(product1, product2);
        }
    }
} 
