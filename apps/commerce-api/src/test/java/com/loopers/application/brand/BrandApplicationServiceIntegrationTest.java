package com.loopers.application.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.infrastructure.brand.BrandJpaRepository;
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
public class BrandApplicationServiceIntegrationTest {

    @Autowired
    private BrandApplicationService brandApplicationService;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

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
    @DisplayName("브랜드 목록 조회 시,")
    class GetBrandList {

        @Nested
        @DisplayName("브랜드 목록이 존재할 때")
        class Get_BrandListWithBrands {

            @Test
            @DisplayName("브랜드 전체 목록을 조회할 수 있다.")
            void getBrandList() {
                // given
                BrandModel brand1 = BrandModel.of("Nike", "스포츠 브랜드");
                BrandModel brand2 = BrandModel.of("Adidas", "스포츠 브랜드");
                BrandModel brand3 = BrandModel.of("Apple", "전자 브랜드");
                BrandModel brand4 = BrandModel.of("Hyundai", "자동차 브랜드");
                BrandModel brand5 = BrandModel.of("Missha", "화장품 브랜드");
                BrandModel brand6 = BrandModel.of("Zara", "의류 브랜드");
                brandJpaRepository.save(brand1);
                brandJpaRepository.save(brand2);
                brandJpaRepository.save(brand3);
                brandJpaRepository.save(brand4);
                brandJpaRepository.save(brand5);
                brandJpaRepository.save(brand6);

                // when
                List<BrandModel> brands = brandApplicationService.getBrandList();

                // then
                assertThat(brands).hasSize(6);
                assertThat(brands).extracting("name").containsExactlyInAnyOrder("Nike", "Adidas", "Apple", "Hyundai", "Missha", "Zara");
            }
        }

        @Nested
        @DisplayName("브랜드 목록이 존재하지 않을 때")
        class Get_BrandListWithoutBrands {
            @Test
            @DisplayName("빈 목록을 반환한다.")
            void getBrandListWhenNoBrands() {
                // when
                List<BrandModel> brands = brandApplicationService.getBrandList();

                // then
                assertThat(brands).isEmpty();
            }
        }

    }

    @Nested
    @DisplayName("브랜드 상세 조회 시,")
    class GetBrandDetail {

        @Nested
        @DisplayName("존재하는 브랜드 ID로 상세 조회하는 경우,")
        class Get_BrandDetailWithValidId {

            @Test
            @DisplayName("해당 브랜드 정보와 연관된 상품 목록을 조회할 수 있다.")
            void getBrandDetailWithValidId() {
                // given
                BrandModel brand1 = BrandModel.of("Nike", "스포츠 브랜드");
                BrandModel savedBrand1 = brandJpaRepository.save(brand1);
                
                BrandModel brand2 = BrandModel.of("Adidas", "스포츠 브랜드");
                BrandModel savedBrand2 = brandJpaRepository.save(brand2);
                
                BrandModel brand3 = BrandModel.of("Apple", "전자 브랜드");
                BrandModel savedBrand3 = brandJpaRepository.save(brand3);

                ProductModel product1 = ProductModel.builder()
                    .brandId(savedBrand1.getId())
                    .categoryId(1L)
                    .name("Nike Air Max")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product2 = ProductModel.builder()
                    .brandId(savedBrand1.getId())
                    .categoryId(1L)
                    .name("Nike Air Force")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product3 = ProductModel.builder()
                    .brandId(savedBrand2.getId())
                    .categoryId(1L)
                    .name("Adidas Superstar")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product4 = ProductModel.builder()
                    .brandId(savedBrand3.getId())
                    .categoryId(2L)
                    .name("Apple iPhone 15")
                    .description("스마트폰")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product5 = ProductModel.builder()
                    .brandId(999L)
                    .categoryId(3L)
                    .name("Dyson")
                    .description("청소기")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();
                ProductModel product6 = ProductModel.builder()
                    .brandId(savedBrand1.getId())
                    .categoryId(1L)
                    .name("Nike Dunk")
                    .description("스포츠 신발")
                    .price(100000)
                    .stock(100)
                    .likesCount(0)
                    .build();

                productJpaRepository.save(product1);
                productJpaRepository.save(product2);
                productJpaRepository.save(product3);
                productJpaRepository.save(product4);
                productJpaRepository.save(product5);
                productJpaRepository.save(product6);

                // when
                BrandModel brand = brandApplicationService.getBrandDetail(savedBrand1.getId());

                // then
                assertThat(brand.getName()).isEqualTo(savedBrand1.getName());
                assertThat(brandApplicationService.getProductList(savedBrand1.getId())).hasSize(3);
            }

            @Test
            @DisplayName("해당 브랜드에 속한 상품이 없으면 빈 목록을 반환한다.")
            void getBrandDetailWithValidIdWithoutProducts() {
                // given
                BrandModel brand1 = BrandModel.of("Nike", "스포츠 브랜드");
                BrandModel savedBrand1 = brandJpaRepository.save(brand1);

                // when
                BrandModel brand = brandApplicationService.getBrandDetail(savedBrand1.getId());

                // then
                assertThat(brandApplicationService.getProductList(savedBrand1.getId())).isEmpty();
            }

        }

        @Nested
        @DisplayName("존재하지 않는 브랜드 ID로 상세 조회 시,")
        class Get_BrandDetailWithInvalidId {

            @Test
            @DisplayName("예외가 발생한다.")
            void getBrandDetailWithInvalidId() {
                // when & then
                assertThatThrownBy(() -> brandApplicationService.getBrandDetail(100L))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
            }
        }

    }

}
