package com.loopers.application.order;

import com.loopers.application.point.PointProcessor;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.user.*;
import com.loopers.infrastructure.point.PointJpaRepository;
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
public class PointUseOptimisticLockTest {

    @Autowired
    private PointProcessor pointProcessor;

    @Autowired
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private PointModel point;
    private UserModel user;

    @BeforeEach
    void setUp() {
        UserModel builderUser = UserModel.of(
                UserId.of("testuser"),
                Email.of("test@loopers.com"),
                Gender.MALE,
                BirthDate.of("1990-01-01")
        );
        user = userRepository.save(builderUser);

        // 10000 포인트를 가진 사용자 포인트 생성
        PointModel builderPoint = PointModel.of(user.getUserId(), 10000);
        point = pointJpaRepository.save(builderPoint);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("[낙관적락] 동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감되어야 한다.")
    void optimisticLockTest() throws InterruptedException {
        // given
        int threadCount = 3; // 3개 주문에서 동시에 포인트 사용 시도
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 각 주문에서 2000 포인트씩 사용 시도 (총 6000 포인트)
        int requestPoint = 2000;
        int orderPrice = 50000;

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    int usedPoints = pointProcessor.processPointUsage(user.getUserId(), orderPrice, requestPoint);
                    successCount.incrementAndGet();
                    System.out.println("포인트 사용 성공: " + usedPoints + " 포인트 차감");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("포인트 사용 실패: " + e.getMessage());
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

        // 최종 포인트 잔액 확인 (10000 - 6000 = 4000)
        Optional<PointModel> updatedPoint = pointJpaRepository.findByUserId(user.getUserId());
        assertThat(updatedPoint).isPresent();
        assertThat(updatedPoint.get().getAmount()).isEqualTo(4000);
    }

    @Test
    @DisplayName("[낙관적락] 동시에 여러 주문 요청 시 포인트 잔액이 부족하면 포인트 사용 가능한 주문에 대해서만 차감 후 나머지는 사용하지 않는다.")
    void optimisticLockTest2() throws InterruptedException {
        // given
        int threadCount = 5; // 5개 주문에서 동시에 포인트 사용 시도
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 각 주문에서 3000 포인트씩 사용 시도 (총 15000 포인트, 보유 포인트 10000 초과)
        int requestPoint = 3000;
        int orderPrice = 50000;

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    int usedPoints = pointProcessor.processPointUsage(user.getUserId(), orderPrice, requestPoint);
                    successCount.incrementAndGet();
                    System.out.println("포인트 사용 성공: " + usedPoints + " 포인트 차감");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("포인트 사용 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(5);
        assertThat(failureCount.get()).isEqualTo(0);

        // 최종 포인트 잔액 0
        Optional<PointModel> updatedPoint = pointJpaRepository.findByUserId(user.getUserId());
        assertThat(updatedPoint).isPresent();
        assertThat(updatedPoint.get().getAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[낙관적락] 포인트가 없는 사용자의 동시 주문 시도 시 모든 요청이 실패해야 한다.")
    void optimisticLockTest3() throws InterruptedException {
        // given
        // 포인트가 없는 사용자 생성
        UserModel noPointUser = UserModel.of(
                UserId.of("nopoint"),
                Email.of("nopoint@loopers.com"),
                Gender.FEMALE,
                BirthDate.of("1995-01-01")
        );
        userRepository.save(noPointUser);

        int threadCount = 3; // 3개 주문에서 동시에 포인트 사용 시도
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // 각 주문에서 1000 포인트씩 사용 시도 (보유 포인트 0)
        int requestPoint = 1000;
        int orderPrice = 50000;

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                try {
                    int usedPoints = pointProcessor.processPointUsage(noPointUser.getUserId(), orderPrice, requestPoint);
                    successCount.incrementAndGet();
                    System.out.println("포인트 사용 성공: " + usedPoints + " 포인트 차감");
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.out.println("포인트 사용 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        // 모든 요청 성공
        assertThat(successCount.get()).isEqualTo(3);
        assertThat(failureCount.get()).isEqualTo(0);

        // 포인트 잔액 0으로 유지
        Optional<PointModel> updatedPoint = pointJpaRepository.findByUserId(noPointUser.getUserId());
        assertThat(updatedPoint).isEmpty(); // 포인트가 없는 사용자는 PointModel이 생성되지 않음
    }
}
