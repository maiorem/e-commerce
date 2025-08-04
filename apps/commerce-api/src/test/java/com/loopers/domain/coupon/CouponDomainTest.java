package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponDomainTest {

    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);
    private final LocalDate yesterday = today.minusDays(1);

    @Test
    @DisplayName("정상적인 쿠폰을 생성할 수 있다.")
    void createCoupon_Success() {
        // given & when
        CouponModel coupon = CouponModel.builder()
                .name("신규 가입 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // then
        assertThat(coupon.getName()).isEqualTo("신규 가입 쿠폰");
        assertThat(coupon.getType()).isEqualTo(CouponType.FIXED_AMOUNT);
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(coupon.getDiscountValue()).isEqualTo(5000);
        assertThat(coupon.getMinimumOrderAmount()).isEqualTo(10000);
        assertThat(coupon.getMaximumDiscountAmount()).isEqualTo(5000);
        assertThat(coupon.getIssuedAt()).isEqualTo(today);
        assertThat(coupon.getValidUntil()).isEqualTo(tomorrow);
        assertThat(coupon.getCouponCode()).isNotNull();
    }

    @Test
    @DisplayName("쿠폰 이름이 null이면 예외가 발생한다.")
    void createCoupon_WithNullName_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> CouponModel.builder()
                .name(null)
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build())
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("쿠폰 타입이 null이면 예외가 발생한다.")
    void createCoupon_WithNullType_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> CouponModel.builder()
                .name("신규 가입 쿠폰")
                .type(null)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build())
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("할인 금액이 음수이면 예외가 발생한다.")
    void createCoupon_WithNegativeDiscountValue_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> CouponModel.builder()
                .name("신규 가입 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(-1000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build())
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("최소 주문 금액이 음수이면 예외가 발생한다.")
    void createCoupon_WithNegativeMinimumOrderAmount_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> CouponModel.builder()
                .name("신규 가입 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(-1000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build())
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("최대 할인 금액이 음수이면 예외가 발생한다.")
    void createCoupon_WithNegativeMaximumDiscountAmount_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> CouponModel.builder()
                .name("신규 가입 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(-1000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build())
                .isInstanceOf(CoreException.class);
    }

    @Test
    @DisplayName("정액 할인 쿠폰의 할인 금액을 계산할 수 있다.")
    void calculateDiscount_FixedAmountCoupon_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("정액 할인 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // when
        int discount = coupon.calculateDiscount(15000);

        // then
        assertThat(discount).isEqualTo(5000);
    }

    @Test
    @DisplayName("정액 할인 쿠폰에서 최소 주문 금액 미달 시 할인이 적용되지 않는다.")
    void calculateDiscount_FixedAmountCoupon_BelowMinimumOrderAmount_ReturnsZero() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("정액 할인 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // when
        int discount = coupon.calculateDiscount(5000);

        // then
        assertThat(discount).isEqualTo(0);
    }

    @Test
    @DisplayName("정액 할인 쿠폰에서 최대 할인 금액을 초과하지 않는다.")
    void calculateDiscount_FixedAmountCoupon_RespectsMaximumDiscountAmount() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("정액 할인 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(10000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // when
        int discount = coupon.calculateDiscount(15000);

        // then
        assertThat(discount).isEqualTo(5000); // 최대 할인 금액으로 제한
    }

    @Test
    @DisplayName("정률 할인 쿠폰의 할인 금액을 계산할 수 있다.")
    void calculateDiscount_PercentageCoupon_Success() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("정률 할인 쿠폰")
                .type(CouponType.PERCENTAGE)
                .discountValue(20) // 20% 할인
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(10000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // when
        int discount = coupon.calculateDiscount(50000);

        // then
        assertThat(discount).isEqualTo(10000); // 50000 * 0.2 = 10000
    }

    @Test
    @DisplayName("정률 할인 쿠폰에서 최대 할인 금액을 초과하지 않는다.")
    void calculateDiscount_PercentageCoupon_RespectsMaximumDiscountAmount() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("정률 할인 쿠폰")
                .type(CouponType.PERCENTAGE)
                .discountValue(30) // 30% 할인
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // when
        int discount = coupon.calculateDiscount(50000);

        // then
        assertThat(discount).isEqualTo(5000); // 최대 할인 금액으로 제한 (50000 * 0.3 = 15000이지만 5000으로 제한)
    }

    @Test
    @DisplayName("정률 할인 쿠폰에서 최소 주문 금액 미달 시 할인이 적용되지 않는다.")
    void calculateDiscount_PercentageCoupon_BelowMinimumOrderAmount_ReturnsZero() {
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

        // when
        int discount = coupon.calculateDiscount(5000);

        // then
        assertThat(discount).isEqualTo(0);
    }

    @Test
    @DisplayName("유효한 쿠폰은 true를 반환한다.")
    void isValid_ValidCoupon_ReturnsTrue() {
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

        // when
        boolean isValid = coupon.isValid(15000, today);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("최소 주문 금액 미달 시 false를 반환한다.")
    void isValid_BelowMinimumOrderAmount_ReturnsFalse() {
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

        // when
        boolean isValid = coupon.isValid(5000, today);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("유효기간이 지난 쿠폰은 false를 반환한다.")
    void isValid_ExpiredCoupon_ReturnsFalse() {
        // given
        CouponModel coupon = CouponModel.builder()
                .name("만료된 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(yesterday)
                .build();

        // when
        boolean isValid = coupon.isValid(15000, today);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("유효기간이 null인 쿠폰은 true를 반환한다.")
    void isValid_NullValidUntil_ReturnsTrue() {
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

        // when
        boolean isValid = coupon.isValid(15000, today);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("쿠폰 코드가 자동으로 생성된다.")
    void createCoupon_CouponCodeIsGenerated() {
        // given & when
        CouponModel coupon1 = CouponModel.builder()
                .name("쿠폰1")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        CouponModel coupon2 = CouponModel.builder()
                .name("쿠폰2")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(tomorrow)
                .build();

        // then
        assertThat(coupon1.getCouponCode()).isNotNull();
        assertThat(coupon2.getCouponCode()).isNotNull();
        assertThat(coupon1.getCouponCode()).isNotEqualTo(coupon2.getCouponCode());
    }
} 
