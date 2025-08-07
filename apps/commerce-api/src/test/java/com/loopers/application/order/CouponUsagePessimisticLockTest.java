package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.user.*;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.coupon.UserCouponJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CouponUsagePessimisticLockTest {

    @Autowired
    private CouponProcessor couponProcessor;

    @Autowired
    private CouponJpaRepository couponRepository;

    @Autowired
    private UserCouponJpaRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private CouponModel coupon;
    private UserModel user;
    private UserCouponModel userCoupon;

    @BeforeEach
    void setUp() {
        UserModel builderUser = UserModel.of(
                UserId.of("seyoung"),
                Email.of("seyoung@loopers.com"),
                Gender.FEMALE,
                BirthDate.of("2000-01-01")
        );
        user = userRepository.save(builderUser);

        CouponModel buildCoupont = CouponModel.builder()
                .name("TEST_COUPON")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(15000)
                .issuedAt(LocalDate.now())
                .validUntil(LocalDate.now().plusDays(30))
                .build();
        coupon = couponRepository.save(buildCoupont);

        UserCouponModel builderUserCoupon = UserCouponModel.create(UserId.of(user.getUserId().getValue()), coupon.getCouponCode());
        userCoupon = userCouponRepository.save(builderUserCoupon);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("[비관적락] 동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다.")
    void pessimisticLockTest() throws InterruptedException {
        // given
        int threadCount = 5; // 5개 기기에서 동시에 쿠폰 사용 시도
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 1; i <= threadCount; i++) {
            executorService.submit(() -> {
                transactionTemplate.execute(status -> {
                    try {
                        couponProcessor.useCoupon(user.getUserId(), coupon.getCouponCode());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                    return null;
                });
            });

        }
        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(1); // 한번만 성공해야 함

        // 트랜잭션 내에서 비관적 락으로 조회
        Optional<UserCouponModel> updatedUserCoupon = transactionTemplate.execute(status -> 
            userCouponRepository.findByUserIdAndCouponCode(user.getUserId(), coupon.getCouponCode())
        );
        assertThat(updatedUserCoupon).isPresent();
        assertThat(updatedUserCoupon.get().isUsed()).isTrue();

    }
}
