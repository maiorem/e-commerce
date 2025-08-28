package com.loopers.application.product;


import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductSortBy;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.loopers.testcontainers.MySqlTestContainersConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(MySqlTestContainersConfig.class)
public class ProductApplicationServiceIntegrationTest {

    @Autowired
    private ProductApplicationService productApplicationService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }


    @Nested
    @DisplayName("상품 목록 조회 시,")
    class Get_ProductList {

        @Nested
        @DisplayName("상품 목록 조회 시")
        class Existing_ProductList {

            @Test
            @DisplayName("목록이 존재한다면 인기순으로 목록을 조회할 수 있다.")
            void getProductList() {
                // given
                ProductModel product1 = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(1000)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(2L)
                    .categoryId(2L)
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(500)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(3L)
                    .categoryId(1L)
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(1)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(4L)
                    .categoryId(2L)
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(1500)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(5L)
                    .categoryId(3L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(25)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(6L)
                    .categoryId(2L)
                    .name("LG OLED TV")
                    .description("고급 TV")
                    .price(2000000)
                    .stock(50)
                    .likesCount(590)
                    .build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                ProductQuery query = ProductQuery.from(null, null, null, null, 20, null, null, null, null);
                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList).hasSize(6);
                assertThat(productList.get(0).likeCount()).isGreaterThanOrEqualTo(productList.get(1).likeCount());
            }

            @Test
            @DisplayName("가격이 낮은 순으로 정렬된 목록을 조회할 수 있다.")
            void getProductListByPriceAsc() {
                // given
                ProductModel product1 = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(50000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(2L)
                    .categoryId(2L)
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(3L)
                    .categoryId(1L)
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(4L)
                    .categoryId(2L)
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(5L)
                    .categoryId(3L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(6L)
                    .categoryId(2L)
                    .name("LG OLED TV")
                    .description("고급 TV")
                    .price(2000000)
                    .stock(50)
                    .likesCount(0)
                    .build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                ProductQuery query = ProductQuery.from(null, null, null, ProductSortBy.PRICE_ASC, 20, null, null, null, null);
                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList).hasSize(6);
                assertThat(productList.get(0).price()).isLessThanOrEqualTo(productList.get(1).price());
            }

            @Test
            @DisplayName("가격이 높은 순으로 정렬된 목록을 조회할 수 있다.")
            void getProductListByPriceDesc() {

                // given
                ProductModel product1 = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(2L)
                    .categoryId(2L)
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(3L)
                    .categoryId(1L)
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(4L)
                    .categoryId(2L)
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(5L)
                    .categoryId(3L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(6L)
                    .categoryId(2L)
                    .name("LG OLED TV")
                    .description("고급 TV")
                    .price(2000000)
                    .stock(50)
                    .likesCount(0)
                    .build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                ProductQuery query = ProductQuery.from(null, null, null, ProductSortBy.PRICE_DESC, 20, null, null, null, null);
                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList).hasSize(6);
                // 가격 내림차순 정렬이므로 첫 번째 상품이 가장 비싸야 함
                assertThat(productList.get(0).price()).isGreaterThanOrEqualTo(productList.get(1).price());
            }

            @Test
            @DisplayName("카테고리별로 상품 목록을 조회할 수 있다.")
            void getProductListByCategory() {
                // given
                CategoryModel categoryModel = CategoryModel.of("스포츠", "스포츠 용품");
                CategoryModel saved = categoryJpaRepository.save(categoryModel);
                ProductModel product1 = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(saved.getId())
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(2L)
                    .categoryId(101L)
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(3L)
                    .categoryId(saved.getId())
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(4L)
                    .categoryId(101L)
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(5L)
                    .categoryId(103L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(6L)
                    .categoryId(101L)
                    .name("LG OLED TV")
                    .description("고급 TV")
                    .price(2000000)
                    .stock(50)
                    .likesCount(0)
                    .build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                ProductQuery query = ProductQuery.from(null, null, saved.getId(), null, 20, null, null, null, null);
                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList).hasSize(2);            
                assertThat(productList).allMatch(product -> product.categoryName() != null);
            }

            @Test
            @DisplayName("브랜드별로 상품 목록을 조회할 수 있다.")
            void getProductListByBrand() {
                // given
                BrandModel brandModel = BrandModel.of("Apple", "애플");
                BrandModel saved = brandJpaRepository.save(brandModel);

                ProductModel product1 = ProductModel.builder()
                    .brandId(101L)
                    .categoryId(1L)
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(102L)
                    .categoryId(2L)
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(saved.getId())
                    .categoryId(2L)
                    .name("Apple Mac book pro")
                    .description("맥북 프로")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(saved.getId())
                    .categoryId(2L)
                    .name("Apple iPhone 15")
                    .description("애플 스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(103L)
                    .categoryId(3L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(102L)
                    .categoryId(2L)
                    .name("LG OLED TV")
                    .description("고급 TV")
                    .price(2000000)
                    .stock(50)
                    .likesCount(0)
                    .build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                ProductQuery query = ProductQuery.from(null, saved.getId(), null, null, 20, null, null, null, null);
                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList).hasSize(2);
                assertThat(productList).allMatch(product -> product.brandName() != null);
            }

            @Test
            @DisplayName("상품 이름으로 검색된 목록을 조회할 수 있다.")
            void getProductListByName() {
                // given
                ProductModel product1 = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(2L)
                    .categoryId(2L)
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(3L)
                    .categoryId(1L)
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(4L)
                    .categoryId(2L)
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(5L)
                    .categoryId(3L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(6L)
                    .categoryId(2L)
                    .name("LG OLED TV")
                    .description("고급 TV")
                    .price(2000000)
                    .stock(50)
                    .likesCount(0)
                    .build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                ProductQuery query = ProductQuery.from("Nike", null, null, null, 20, null, null, null, null);
                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList).hasSize(1);
                assertThat(productList.get(0).name()).contains("Nike");
            }

            @Test
            @DisplayName("상품 목록이 존재하지 않을 때 빈 목록을 반환한다.")
            void getEmptyProductList() {
                // given
                ProductQuery query = ProductQuery.from(null, null, null, null, 20, null, null, null, null);

                // when
                List<ProductOutputInfo> productList = productApplicationService.getProductList(query);

                // then
                assertThat(productList).isEmpty();
            }
        }

        @Nested
        @DisplayName("상품 목록 상세 조회 시,")
        class Get_ProductDetail {

            @Test
            @DisplayName("상품 ID가 존재할 때 상품 상세 정보를 조회할 수 있다.")
            void getProductDetail() {
                // given
                ProductModel product = ProductModel.builder()
                        .brandId(1L)
                        .categoryId(1L)
                        .name("Nike Air")
                        .description("스포츠 신발")
                        .price(100000)
                        .stock(100)
                        .likesCount(10)
                        .build();
                ProductModel saved = productJpaRepository.save(product);

                // when
                ProductOutputInfo productDetail = productApplicationService.getProductDetail(saved.getId(), null);

                // then
                assertThat(productDetail).isNotNull();
                assertThat(productDetail.id()).isEqualTo(saved.getId());
                assertThat(productDetail.name()).isEqualTo("Nike Air");
            }

            @Test
            @DisplayName("상품 ID가 존재하지 않을 때 예외를 발생시킨다.")
            void getProductDetailNotFound() {
                // when & then
                assertThatThrownBy(() -> productApplicationService.getProductDetail(999L, null))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @Test
            @DisplayName("상품 상세 정보에 연관된 카테고리와 브랜드 정보를 포함한다.")
            void getProductDetailWithCategoryAndBrand() {
                // given
                BrandModel brand = BrandModel.of("Nike", "나이키");
                BrandModel savedBrand = brandJpaRepository.save(brand);
                CategoryModel category = CategoryModel.of("스포츠", "스포츠 용품");
                CategoryModel savedCategory = categoryJpaRepository.save(category);
                ProductModel product = ProductModel.builder()
                        .brandId(savedBrand.getId())
                        .categoryId(savedCategory.getId())
                        .name("Nike Air")
                        .description("스포츠 신발")
                        .price(100000)
                        .stock(100)
                        .likesCount(10)
                        .build();
                ProductModel savedProduct = productJpaRepository.save(product);

                // when
                ProductOutputInfo productDetail = productApplicationService.getProductDetail(savedProduct.getId(), null);

                // then
                assertThat(productDetail).isNotNull();
                assertThat(productDetail.id()).isEqualTo(savedProduct.getId());
                assertThat(productDetail.brandName()).isEqualTo(savedBrand.getName());
                assertThat(productDetail.categoryName()).isEqualTo(savedCategory.getName());
            }

            @Test
            @DisplayName("상품 상세 정보에 연관된 좋아요 수를 포함한다.")
            void getProductDetailWithLikeCount() {
                // given
                ProductModel product = ProductModel.builder()
                        .brandId(1L)
                        .categoryId(1L)
                        .name("Nike Air")
                        .description("스포츠 신발")
                        .price(100000)
                        .stock(100)
                        .likesCount(3)
                        .build();
                ProductModel savedProduct = productJpaRepository.save(product);
                System.out.println("After save - Saved Product likesCount: " + savedProduct.getLikesCount()); // Diagnostic

                // when
                ProductOutputInfo productDetail = productApplicationService.getProductDetail(savedProduct.getId(), null);
                System.out.println("After service call - ProductDetail likesCount: " + productDetail.likeCount()); // Diagnostic

                // then
                assertThat(productDetail).isNotNull();
                assertThat(productDetail.likeCount()).isEqualTo(3);
            }

        }

    }

}
