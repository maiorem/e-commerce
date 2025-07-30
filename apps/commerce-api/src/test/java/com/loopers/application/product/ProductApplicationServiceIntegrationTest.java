package com.loopers.application.product;


import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.like.LikeModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductSortBy;
import com.loopers.domain.user.UserId;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
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
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Nested
    @DisplayName("상품 목록 조회 시,")
    class Get_ProductList {

        @Nested
        @DisplayName("상품 목록 조회 시")
        class Existing_ProductList {

            @Test
            @DisplayName("목록이 존재한다면 최신 등록순으로 목록을 조회할 수 있다.")
            void getProductList() {
                // given
                ProductModel product1 = ProductModel.builder().brandId(1L).categoryId(1L).name("Nike Air").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product2 = ProductModel.builder().brandId(2L).categoryId(2L).name("Samsung Galaxy S23").description("스마트폰").price(100000).stock(100).build();
                ProductModel product3 = ProductModel.builder().brandId(3L).categoryId(1L).name("Adidas Superstar").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product4 = ProductModel.builder().brandId(4L).categoryId(2L).name("Apple iPhone 15").description("스마트폰").price(100000).stock(100).build();
                ProductModel product5 = ProductModel.builder().brandId(5L).categoryId(3L).name("Dyson").description("청소기").price(100000).stock(100).build();
                ProductModel product6 = ProductModel.builder().brandId(6L).categoryId(2L).name("LG OLED TV").description("고급 TV").price(2000000).stock(50).build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from(null, null, null, null, pageable.getPageNumber(), pageable.getPageSize());
                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList.getContent()).hasSize(6);
            }

            @Test
            @DisplayName("가격이 낮은 순으로 정렬된 목록을 조회할 수 있다.")
            void getProductListByPriceAsc() {
                // given
                ProductModel product1 = ProductModel.builder().brandId(1L).categoryId(1L).name("Nike Air").description("스포츠 신발").price(50000).stock(100).build();
                ProductModel product2 = ProductModel.builder().brandId(2L).categoryId(2L).name("Samsung Galaxy S23").description("스마트폰").price(100000).stock(100).build();
                ProductModel product3 = ProductModel.builder().brandId(3L).categoryId(1L).name("Adidas Superstar").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product4 = ProductModel.builder().brandId(4L).categoryId(2L).name("Apple iPhone 15").description("스마트폰").price(100000).stock(100).build();
                ProductModel product5 = ProductModel.builder().brandId(5L).categoryId(3L).name("Dyson").description("청소기").price(100000).stock(100).build();
                ProductModel product6 = ProductModel.builder().brandId(6L).categoryId(2L).name("LG OLED TV").description("고급 TV").price(2000000).stock(50).build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from(null, null, null, ProductSortBy.PRICE_ASC, pageable.getPageNumber(), pageable.getPageSize());
                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList.getContent()).hasSize(6);
                assertThat(productList.getContent().get(0).price()).isEqualTo(50000);
            }

            @Test
            @DisplayName("가격이 높은 순으로 정렬된 목록을 조회할 수 있다.")
            void getProductListByPriceDesc() {

                // given
                ProductModel product1 = ProductModel.builder().brandId(1L).categoryId(1L).name("Nike Air").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product2 = ProductModel.builder().brandId(2L).categoryId(2L).name("Samsung Galaxy S23").description("스마트폰").price(100000).stock(100).build();
                ProductModel product3 = ProductModel.builder().brandId(3L).categoryId(1L).name("Adidas Superstar").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product4 = ProductModel.builder().brandId(4L).categoryId(2L).name("Apple iPhone 15").description("스마트폰").price(100000).stock(100).build();
                ProductModel product5 = ProductModel.builder().brandId(5L).categoryId(3L).name("Dyson").description("청소기").price(100000).stock(100).build();
                ProductModel product6 = ProductModel.builder().brandId(6L).categoryId(2L).name("LG OLED TV").description("고급 TV").price(2000000).stock(50).build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from(null, null, null, ProductSortBy.PRICE_DESC, pageable.getPageNumber(), pageable.getPageSize());
                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList.getContent()).hasSize(6);
                assertThat(productList.getContent().get(0).price()).isEqualTo(2000000);

            }

            @Test
            @DisplayName("카테고리별로 상품 목록을 조회할 수 있다.")
            void getProductListByCategory() {
                // given
                CategoryModel categoryModel = CategoryModel.of("스포츠", "스포츠 용품");
                CategoryModel saved = categoryJpaRepository.save(categoryModel);
                ProductModel product1 = ProductModel.builder().brandId(1L).categoryId(saved.getId()).name("Nike Air").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product2 = ProductModel.builder().brandId(2L).categoryId(101L).name("Samsung Galaxy S23").description("스마트폰").price(100000).stock(100).build();
                ProductModel product3 = ProductModel.builder().brandId(3L).categoryId(saved.getId()).name("Adidas Superstar").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product4 = ProductModel.builder().brandId(4L).categoryId(101L).name("Apple iPhone 15").description("스마트폰").price(100000).stock(100).build();
                ProductModel product5 = ProductModel.builder().brandId(5L).categoryId(103L).name("Dyson").description("청소기").price(100000).stock(100).build();
                ProductModel product6 = ProductModel.builder().brandId(6L).categoryId(101L).name("LG OLED TV").description("고급 TV").price(2000000).stock(50).build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from(null, null, "스포츠", ProductSortBy.PRICE_ASC, pageable.getPageNumber(), pageable.getPageSize());
                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList.getContent()).hasSize(2);
                assertThat(productList.getContent().get(0).name()).isEqualTo("Nike Air");
                assertThat(productList.getContent().get(1).name()).isEqualTo("Adidas Superstar");
            }

            @Test
            @DisplayName("브랜드별로 상품 목록을 조회할 수 있다.")
            void getProductListByBrand() {
                // given
                BrandModel brandModel = BrandModel.of("Apple", "애플");
                BrandModel saved = brandJpaRepository.save(brandModel);

                ProductModel product1 = ProductModel.builder().brandId(101L).categoryId(1L).name("Nike Air").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product2 = ProductModel.builder().brandId(102L).categoryId(2L).name("Samsung Galaxy S23").description("스마트폰").price(100000).stock(100).build();
                ProductModel product3 = ProductModel.builder().brandId(saved.getId()).categoryId(2L).name("Apple Mac book pro").description("맥북 프로").price(100000).stock(100).build();
                ProductModel product4 = ProductModel.builder().brandId(saved.getId()).categoryId(2L).name("Apple iPhone 15").description("애플 스마트폰").price(100000).stock(100).build();
                ProductModel product5 = ProductModel.builder().brandId(103L).categoryId(3L).name("Dyson").description("청소기").price(100000).stock(100).build();
                ProductModel product6 = ProductModel.builder().brandId(102L).categoryId(2L).name("LG OLED TV").description("고급 TV").price(2000000).stock(50).build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from(null, "Apple", null, null, pageable.getPageNumber(), pageable.getPageSize());
                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList.getContent()).hasSize(2);
                assertThat(productList.getContent().get(0).brandId()).isEqualTo(product3.getBrandId());
            }

            @Test
            @DisplayName("상품 이름으로 검색된 목록을 조회할 수 있다.")
            void getProductListByName() {
                // given
                ProductModel product1 = ProductModel.builder().brandId(1L).categoryId(1L).name("Nike Air").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product2 = ProductModel.builder().brandId(2L).categoryId(2L).name("Samsung Galaxy S23").description("스마트폰").price(100000).stock(100).build();
                ProductModel product3 = ProductModel.builder().brandId(3L).categoryId(1L).name("Adidas Superstar").description("스포츠 신발").price(100000).stock(100).build();
                ProductModel product4 = ProductModel.builder().brandId(4L).categoryId(2L).name("Apple iPhone 15").description("스마트폰").price(100000).stock(100).build();
                ProductModel product5 = ProductModel.builder().brandId(5L).categoryId(3L).name("Dyson").description("청소기").price(100000).stock(100).build();
                ProductModel product6 = ProductModel.builder().brandId(6L).categoryId(2L).name("LG OLED TV").description("고급 TV").price(2000000).stock(50).build();
                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from("OLED", null, null, null, pageable.getPageNumber(), pageable.getPageSize());
                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

                // then
                assertThat(productList).isNotNull();
                assertThat(productList.getContent()).hasSize(1);
                assertThat(productList.getContent().get(0).name()).isEqualTo("LG OLED TV");
            }

            @Test
            @DisplayName("상품 목록이 존재하지 않을 때 빈 목록을 반환한다.")
            void getEmptyProductList() {
                // given
                Pageable pageable = PageRequest.of(0, 10);
                ProductQuery query = ProductQuery.from(null, null, null, null, pageable.getPageNumber(), pageable.getPageSize());

                // when
                Page<ProductOutputInfo> productList = productApplicationService.getProductList(pageable, query);

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
                ProductOutputInfo productDetail = productApplicationService.getProductDetail(saved.getId());

                // then
                assertThat(productDetail).isNotNull();
                assertThat(productDetail.id()).isEqualTo(product.getId());
                assertThat(productDetail.name()).isEqualTo("Nike Air");
            }


            @Test
            @DisplayName("상품 ID가 존재하지 않을 때 예외를 발생시킨다.")
            void getProductDetailNotFound() {
                // when
                assertThatThrownBy(() -> productApplicationService.getProductDetail(999L))
                        .isInstanceOf(CoreException.class);
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
                ProductOutputInfo productDetail = productApplicationService.getProductDetail(savedProduct.getId());

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
                        .likesCount(0) // 좋아요 수를 3으로 설정
                        .build();
                ProductModel savedProduct = productJpaRepository.save(product);
                LikeModel likeModel1 = LikeModel.of(UserId.of("user1"), savedProduct.getId());
                LikeModel likeModel2 = LikeModel.of(UserId.of("user2"), savedProduct.getId());
                LikeModel likeModel3 = LikeModel.of(UserId.of("user3"), savedProduct.getId());
                likeJpaRepository.save(likeModel1);
                likeJpaRepository.save(likeModel2);
                likeJpaRepository.save(likeModel3);

                // when
                ProductOutputInfo productDetail = productApplicationService.getProductDetail(savedProduct.getId());

                // then
                assertThat(productDetail).isNotNull();
                assertThat(productDetail.likeCount()).isEqualTo(3);
            }

        }

    }

}
