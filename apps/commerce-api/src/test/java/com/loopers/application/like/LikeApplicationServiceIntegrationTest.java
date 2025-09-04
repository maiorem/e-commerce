package com.loopers.application.like;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.*;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.support.config.LikeTestConfig;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Import(LikeTestConfig.class)
class LikeApplicationServiceIntegrationTest {

    @Autowired
    private LikeApplicationService likeApplicationService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private ProductModel product;
    private UserId userId;
    private BrandModel brand;
    private CategoryModel category;
    private UserModel user;

    @Autowired
    private LikeCountProcessor likeCountProcessor;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        
        // Mock 호출 기록 초기화
        reset(likeCountProcessor);
        
        // User 먼저 생성
        userId = UserId.of("seyoung");
        user = UserModel.of(userId, Email.of("seyoung@loopers.com"), Gender.FEMALE, BirthDate.of("1990-01-01"));
        userRepository.save(user);
        
        // Brand와 Category 생성
        brand = BrandModel.of("Apple", "Apple Inc.");
        brandJpaRepository.save(brand);
        
        category = CategoryModel.of("전자제품", "전자제품 카테고리");
        categoryJpaRepository.save(category);
        
        product = ProductModel.builder()
                .brandId(brand.getId())
                .categoryId(category.getId())
                .name("Apple iPhone 14")
                .description("애플 아이폰 14")
                .price(10000)
                .stock(100)
                .likesCount(0)
                .build();
        product = productJpaRepository.saveAndFlush(product);
    }

    @Test
    @DisplayName("중복 좋아요 요청 시 첫 번째 요청만 처리되고 두 번째 요청은 무시된다.")
    void duplicateLikeRequest() {
        // given
        Long productId = product.getId();

        // when - 첫 번째 좋아요 요청
        likeApplicationService.like(userId, productId);

        // then - 첫 번째 요청이 성공적으로 처리됨 (이벤트 기반 비동기이므로 eventually 확인)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
            ProductModel unchangedProduct = productJpaRepository.findById(productId).orElseThrow();
            assertThat(unchangedProduct.getLikesCount()).isEqualTo(1);
        });

        // processor 호출 검증 (비동기 호출)
        verify(likeCountProcessor, timeout(1500)).updateProductLikeCount(productId, userId, 1);

        // when - 두 번째 좋아요 요청 (중복)
        likeApplicationService.like(userId, productId);

        // then - 두 번째 요청부터 상태 변화 없음 (eventually)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
            ProductModel unchangedProduct2 = productJpaRepository.findById(productId).orElseThrow();
            assertThat(unchangedProduct2.getLikesCount()).isEqualTo(1);
        });

        // 중복 요청 후에도 processor는 추가로 호출되지 않음을 확인 (여전히 1번만)
        verify(likeCountProcessor, times(1)).updateProductLikeCount(productId, userId, 1);
    }

    @Test
    @DisplayName("중복 좋아요 취소 요청 시 첫 번째 요청만 처리되고 두 번째 요청은 무시된다.")
    void duplicateUnlikeRequest() {
        // given - 먼저 좋아요를 등록
        Long productId = product.getId();
        likeApplicationService.like(userId, productId);
        assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();

        // when - 첫 번째 좋아요 취소 요청
        likeApplicationService.unlike(userId, productId);

        // then - 첫 번째 요청이 성공적으로 처리됨 (eventually)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isFalse();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(0);
            ProductModel updatedProduct = productJpaRepository.findById(productId).orElseThrow();
            assertThat(updatedProduct.getLikesCount()).isEqualTo(0);
        });

        // processor 호출 검증
        verify(likeCountProcessor, timeout(1500)).updateProductLikeCount(productId, userId,-1);

        // when - 두 번째 좋아요 취소 요청 (중복)
        likeApplicationService.unlike(userId, productId);

        // then - 두 번째 요청은 무시됨 (상태 변화 없음, eventually)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isFalse();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(0);
            ProductModel unchangedProduct3 = productJpaRepository.findById(productId).orElseThrow();
            assertThat(unchangedProduct3.getLikesCount()).isEqualTo(0);
        });

        // 중복 요청 후에도 processor는 추가로 호출되지 않음을 확인 (여전히 1번만)
        verify(likeCountProcessor, times(1)).updateProductLikeCount(productId, userId,-1);
    }

    @Test
    @DisplayName("좋아요 등록 후 취소, 다시 등록이 정상적으로 동작한다.")
    void likeUnlikeLikeSequence() {
        // given
        Long productId = product.getId();

        // when - 좋아요 등록
        likeApplicationService.like(userId, productId);

        // then - 좋아요 등록 확인 (eventually)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
        });

        // when - 좋아요 취소
        likeApplicationService.unlike(userId, productId);

        // then - 좋아요 취소 확인 (eventually)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isFalse();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(0);
        });
        
        // when - 다시 좋아요 등록
        likeApplicationService.like(userId, productId);

        // then - 다시 좋아요 등록 확인 (eventually)
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
            assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
        });
        
    }
} 
