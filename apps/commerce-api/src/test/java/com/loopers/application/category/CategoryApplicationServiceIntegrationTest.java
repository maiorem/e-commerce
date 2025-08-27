package com.loopers.application.category;

import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.support.config.TestConfig;
import com.loopers.testcontainers.MySqlTestContainersConfig;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import({TestConfig.class, MySqlTestContainersConfig.class})
public class CategoryApplicationServiceIntegrationTest {

    @Autowired
    private CategoryApplicationService categoryApplicationService;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

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
    @DisplayName("카테고리 목록 조회 시,")
    class GetCategoryList {

        @Nested
        @DisplayName("카테고리 목록이 존재할 때")
        class Existing_CategoryList {

            @Test
            @DisplayName("목록을 조회할 수 있다.")
            void getCategoryList() {
                // given
                CategoryModel category1 = CategoryModel.of("Electronics", "전자제품");
                CategoryModel category2 = CategoryModel.of("Clothing", "의류");
                categoryJpaRepository.save(category1);
                categoryJpaRepository.save(category2);

                // when
                List<CategoryModel> categoryList = categoryApplicationService.getCategoryList();

                // then
                assertThat(categoryList).hasSize(2);
                assertThat(categoryList).extracting("name").containsExactlyInAnyOrder("Electronics", "Clothing");
            }

        }

    }

    @Nested
    @DisplayName("카테고리 목록 조회 시,")
    class Empty_CategoryList {

        @Test
        @DisplayName("카테고리가 없을 때 빈 목록을 반환한다.")
        void getEmptyCategoryList() {
            // when
            List<CategoryModel> categoryList = categoryApplicationService.getCategoryList();

            // then
            assertThat(categoryList).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리 상세 조회 시,")
    class GetCategoryDetail {

        @Nested
        @DisplayName("카테고리 ID가 존재할 때")
        class Existing_CategoryDetail {

            @Test
            @DisplayName("해당 카테고리의 연관 상품 목록을 조회할 수 있다.")
            void getCategoryDetail() {
                // given
                CategoryModel category = CategoryModel.of("Electronics", "전자제품");
                CategoryModel savedCategory = categoryJpaRepository.save(category);

                ProductModel product1 = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(999L)
                    .name("Nike Air")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(2L)
                    .categoryId(savedCategory.getId())
                    .name("Samsung Galaxy S23")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(3L)
                    .categoryId(999L)
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(4L)
                    .categoryId(savedCategory.getId())
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(5L)
                    .categoryId(999L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(6L)
                    .categoryId(savedCategory.getId())
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

                // when
                CategoryModel categoryDetail = categoryApplicationService.getCategoryDetail(savedCategory.getId());

                // then
                assertThat(categoryDetail).isNotNull();
                assertThat(categoryDetail.getName()).isEqualTo(savedCategory.getName());
                assertThat(categoryApplicationService.getProductList(savedCategory.getId())).hasSize(3);
            }

            @Test
            @DisplayName("해당 카테고리에 속하는 상품 목록이 없을 때, 빈 목록을 반환한다.")
            void getCategoryDetailWithNoProducts() {
                // given
                CategoryModel category = CategoryModel.of("Electronics", "전자제품");
                CategoryModel savedCategory = categoryJpaRepository.save(category);

                // when
                CategoryModel categoryDetail = categoryApplicationService.getCategoryDetail(savedCategory.getId());

                // then
                assertThat(categoryDetail).isNotNull();
                assertThat(categoryApplicationService.getProductList(savedCategory.getId())).isEmpty();
            }

        }

        @Nested
        @DisplayName("존재하지 않는 카테고리 ID로 조회하는 경우")
        class NonExisting_CategoryDetail {

            @Test
            @DisplayName("예외가 발생한다.")
            void getCategoryDetailWithInvalidId() {
                // when & then
                assertThatThrownBy(() -> categoryApplicationService.getCategoryDetail(999L))
                        .isInstanceOf(CoreException.class)
                        .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
            }
        }

    }

}
