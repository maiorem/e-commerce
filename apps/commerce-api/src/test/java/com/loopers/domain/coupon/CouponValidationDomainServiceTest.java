package com.loopers.domain.coupon;

import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponValidationDomainServiceTest {

    private CouponValidationDomainService couponValidationDomainService;
    private LocalDate today;
    private LocalDate tomorrow;
    private LocalDate yesterday;

    @BeforeEach
    void setUp() {
        couponValidationDomainService = new CouponValidationDomainService();
        today = LocalDate.now();
        tomorrow = today.plusDays(1);
        yesterday = today.minusDays(1);
    }

    @Test
    @DisplayName("유효한 쿠폰과 사용자 쿠폰으로 검증이 성공한다.")
    void validateCouponUsage_ValidCouponAndUserCoupon_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("유효한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        // 예외가 발생하지 않으면 성공
        couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 15000, today);
    }

    @Test
    @DisplayName("쿠폰이 유효하지 않으면 예외가 발생한다.")
    void validateCouponUsage_InvalidCoupon_ThrowsException() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("만료된 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(yesterday) // 만료된 쿠폰
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        assertThatThrownBy(() -> couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 15000, today))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("최소 주문 금액 미달 시 예외가 발생한다.")
    void validateCouponUsage_BelowMinimumOrderAmount_ThrowsException() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("유효한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        assertThatThrownBy(() -> couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 5000, today))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰이면 예외가 발생한다.")
    void validateCouponUsage_AlreadyUsedCoupon_ThrowsException() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("유효한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");
        userCoupon.useCoupon(today); // 이미 사용된 쿠폰

        // when & then
        assertThatThrownBy(() -> couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 15000, today))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정률 할인 쿠폰도 정상적으로 검증된다.")
    void validateCouponUsage_PercentageCoupon_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("정률 할인 쿠폰")
                .type(CouponType.PERCENTAGE)
                .discountValue(20)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(10000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 50000, today);
    }

    @Test
    @DisplayName("유효기간이 null인 쿠폰도 정상적으로 검증된다.")
    void validateCouponUsage_NullValidUntil_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("무제한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(null)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 15000, today);
    }

    @Test
    @DisplayName("정확히 최소 주문 금액일 때도 정상적으로 검증된다.")
    void validateCouponUsage_ExactMinimumOrderAmount_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("유효한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 10000, today);
    }

    @Test
    @DisplayName("유효기간 마지막 날에도 정상적으로 검증된다.")
    void validateCouponUsage_OnValidUntilDate_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("유효한 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        couponValidationDomainService.validateCouponUsage(userCoupon, coupon, 15000, tomorrow);
    }
} 
