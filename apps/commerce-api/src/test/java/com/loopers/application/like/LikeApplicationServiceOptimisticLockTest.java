package com.loopers.application.like;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.*;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class LikeApplicationServiceOptimisticLockTest {

    @Autowired
    private LikeApplicationService likeApplicationService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private ProductModel product;
    private BrandModel brand;
    private CategoryModel category;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        
        // Brand와 Category 먼저 생성
        brand = BrandModel.of("테스트브랜드", "테스트 브랜드입니다");
        brandJpaRepository.save(brand);
        
        category = CategoryModel.of("테스트카테고리", "테스트 카테고리입니다");
        categoryJpaRepository.save(category);
        
        // 10명의 사용자를 생성
        for (int i = 1; i <= 10; i++) {
            UserModel user = UserModel.of(
                    UserId.of("seyoung" + i),
                    Email.of("seyoung" + i + "@loopers.com"),
                    Gender.FEMALE,
                    BirthDate.of("1990-01-01")
            );
            userRepository.save(user);
        }
        
        product = ProductModel.builder()
                .brandId(brand.getId())
                .categoryId(category.getId())
                .name("테스트 상품")
                .price(100000)
                .stock(10)
                .description("낙관적 락 테스트용 상품")
                .likesCount(0)
                .build();
        product = productRepository.save(product);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[낙관적락] 동일한 상품에 대해 여러명이 좋아요를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다.")
    void optimisticLockRetryAnnotationTest() throws InterruptedException {
        // given
        int threadCount = 5; // 5명이 동시에 좋아요
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when - 동시에 좋아요 요청 (각 사용자는 한 번씩만)
        for (int i = 1; i <= threadCount; i++) {
            final String userId = "seyoung" + i;
            executorService.submit(() -> {
                try {
                    likeApplicationService.like(UserId.of(userId), product.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    throw new RuntimeException("좋아요 실패", e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failureCount.get()).isEqualTo(0);

        Optional<ProductModel> updatedProduct = productRepository.findById(product.getId());
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getLikesCount()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("[낙관적락] 동일한 상품에 대해 여러명이 좋아요 취소를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다.")
    void optimisticLockRetryAnnotationDecrementTest() throws InterruptedException {
        // given - 먼저 좋아요를 10개로 설정하고 실제로 좋아요를 눌러둠
        for (int i = 1; i <= 10; i++) {
            String userId = "seyoung" + i;
            ProductModel currentProduct = productRepository.findById(product.getId()).orElseThrow();
            likeApplicationService.like(UserId.of(userId), currentProduct.getId());
        }

        // 좋아요 수가 10개인지 확인
        Optional<ProductModel> productAfterLikes = productRepository.findById(product.getId());
        assertThat(productAfterLikes).isPresent();
        assertThat(productAfterLikes.get().getLikesCount()).isEqualTo(10);

        int threadCount = 3; // 3명이 동시에 좋아요 취소
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 1; i <= threadCount; i++) {
            String userId = "seyoung" + i;
            executorService.submit(() -> {
                try {
                    ProductModel currentProduct = productRepository.findById(product.getId()).orElseThrow();
                    likeApplicationService.unlike(UserId.of(userId), currentProduct.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failureCount.get()).isEqualTo(0);

        // 최종 좋아요 수 확인 (10 - 3 = 7)
        Optional<ProductModel> updatedProduct = productRepository.findById(product.getId());
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getLikesCount()).isEqualTo(7);
    }

    @Test
    @DisplayName("낙관적 락 재시도 성능 테스트")
    void optimisticLockRetryPerformanceTest() throws InterruptedException {
        // given
        int threadCount = 10; // 10명이 동시에 좋아요
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // when
        for (int i = 0; i < threadCount; i++) {
            String userId = "seyoung" + (i + 1);
            executorService.submit(() -> {
                try {
                    ProductModel currentProduct = productRepository.findById(product.getId()).orElseThrow();
                    likeApplicationService.like(UserId.of(userId), currentProduct.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // then
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failureCount.get()).isEqualTo(0);

        Optional<ProductModel> updatedProduct = productRepository.findById(product.getId());
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getLikesCount()).isEqualTo(threadCount);

        System.out.println("실행 시간: " + executionTime + "ms"); // 결과 : 651ms
    }
}
