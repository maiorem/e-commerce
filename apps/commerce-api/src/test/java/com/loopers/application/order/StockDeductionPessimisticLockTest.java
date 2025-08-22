package com.loopers.application.order;

import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.order.*;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.category.CategoryModel;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StockDeductionPessimisticLockTest {

    @Autowired
    private StockDeductionProcessor stockDeductionProcessor;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private ProductModel product;
    private List<OrderItemModel> orderItems;
    private BrandModel brand;
    private CategoryModel category;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        // Brand와 Category 먼저 생성
        brand = BrandModel.of("테스트브랜드", "테스트 브랜드입니다");
        brand = brandJpaRepository.saveAndFlush(brand);
        
        category = CategoryModel.of("테스트카테고리", "테스트 카테고리입니다");
        category = categoryJpaRepository.saveAndFlush(category);
        
        // 200개 재고를 가진 상품 생성
        ProductModel builderProduct = ProductModel.builder()
                .brandId(brand.getId())
                .categoryId(category.getId())
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(10000)
                .stock(200)
                .likesCount(0)
                .build();
        product = productRepository.save(builderProduct);

        // 주문 아이템 생성 (각각 10개씩 5번 주문)
        orderItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            OrderItemModel orderItem = OrderItemModel.builder()
                    .orderId(1L)
                    .productId(product.getId())
                    .quantity(10)
                    .priceAtOrder(Money.of(10000))
                    .build();
            orderItems.add(orderItem);
        }
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[비관적락] 동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다.")
    void pessimisticLockTest() throws InterruptedException {
        // given
        int threadCount = 3; // 3개 주문에서 동시에 재고 차감 시도 (10개씩 5개 아이템 3번 주문 = 150개 차감)
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockDeductionProcessor.deductProductStocks(orderItems);
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
        assertThat(successCount.get()).isEqualTo(3);
        assertThat(failureCount.get()).isEqualTo(0);

        Optional<ProductModel> updatedProduct = transactionTemplate.execute(status ->
            productRepository.findByIdForUpdate(product.getId())
        );
        assertThat(updatedProduct).isPresent();
        // 3개의 주문(150개 차감)이 성공했으므로 재고는 50이 되어야 함
        assertThat(updatedProduct.get().getStock()).isEqualTo(50); // 200 - (10 * 5 * 3) = 50
    }

    @Test
    @DisplayName("[비관적락] 재고가 부족한 상황에서 동시 주문 시 일부는 실패해야 한다.")
    void pessimisticLockTest2() throws InterruptedException {
        // given
        final ProductModel lowStockProduct = ProductModel.builder()
                .brandId(brand.getId())
                .categoryId(category.getId())
                .name("재고 부족 상품")
                .description("재고 부족 상품 설명")
                .price(10000)
                .stock(70) // 70개 재고
                .likesCount(0)
                .build();
        ProductModel savedLowStockProduct = productRepository.save(lowStockProduct);

        // 주문 아이템 30개 주문
        orderItems = new ArrayList<>();
        OrderItemModel orderItem = OrderItemModel.builder()
                .orderId(1L)
                .productId(savedLowStockProduct.getId())
                .quantity(30)
                .priceAtOrder(Money.of(10000))
                .build();
        orderItems.add(orderItem);

        int threadCount = 3; // 30개씩 세번 동시 주문 시도 (총 90개 차감 시도)
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockDeductionProcessor.deductProductStocks(orderItems);
                    successCount.incrementAndGet();
                    System.out.println("재고 차감 성공");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("재고 차감 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        // 일부는 성공하고 일부는 실패해야 함 (재고 부족으로 인해)
        assertThat(successCount.get()).isGreaterThan(0);
        assertThat(successCount.get() + failureCount.get()).isEqualTo(threadCount);

        // 최종 재고가 음수가 되지 않아야 함
        Optional<ProductModel> updatedProduct = transactionTemplate.execute(status -> 
            productRepository.findByIdForUpdate(savedLowStockProduct.getId())
        );
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getStock()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("[비관적락] 재고가 정확히 맞는 상황에서 동시 주문 시 일부는 실패해야 한다.")
    void pessimisticLockTest3() throws InterruptedException {
        // given
        // 재고가 50개인 새로운 상품 생성 (정확히 1개 주문만 가능)
        final ProductModel exactStockProduct = ProductModel.builder()
                .brandId(brand.getId())
                .categoryId(category.getId())
                .name("정확한 재고 상품")
                .description("정확한 재고 상품 설명")
                .price(10000)
                .stock(50)
                .likesCount(0)
                .build();
        ProductModel savedExactStockProduct = productRepository.save(exactStockProduct);

        orderItems = new ArrayList<>();
        OrderItemModel orderItem = OrderItemModel.builder()
                .orderId(1L)
                .productId(savedExactStockProduct.getId())
                .quantity(50)
                .priceAtOrder(Money.of(10000))
                .build();
        orderItems.add(orderItem);

        int threadCount = 3; // 3개 주문에서 동시에 재고 차감 시도
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockDeductionProcessor.deductProductStocks(orderItems);
                    successCount.incrementAndGet();
                    System.out.println("재고 차감 성공");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("재고 차감 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        // 정확히 1개만 성공하고 나머지는 실패해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(2);

        // 최종 재고가 0이 되어야 함
        Optional<ProductModel> updatedProduct = transactionTemplate.execute(status -> 
            productRepository.findByIdForUpdate(savedExactStockProduct.getId())
        );
        assertThat(updatedProduct).isPresent();
        assertThat(updatedProduct.get().getStock()).isEqualTo(0);
    }
} 
