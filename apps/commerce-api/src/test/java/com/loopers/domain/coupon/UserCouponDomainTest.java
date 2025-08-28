package com.loopers.domain.coupon;

import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserCouponDomainTest {

    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);
    private final LocalDate yesterday = today.minusDays(1);

    @Test
    @DisplayName("정상적인 사용자 쿠폰을 생성할 수 있다.")
    void createUserCoupon_Success() {
        // given & when
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // then
        assertThat(userCoupon.getUserId()).isEqualTo(UserId.of("seyoung"));
        assertThat(userCoupon.getCouponCode()).isEqualTo("COUPON123");
        assertThat(userCoupon.getStatus()).isEqualTo(UserCoupontStatus.AVAILABLE);
        assertThat(userCoupon.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("사용자 ID가 null이면 예외가 발생한다.")
    void createUserCoupon_WithNullUserId_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> UserCouponModel.create(null, "COUPON123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("쿠폰 코드가 null이면 예외가 발생한다.")
    void createUserCoupon_WithNullCouponCode_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> UserCouponModel.create(UserId.of("seyoung"), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("쿠폰 코드가 빈 문자열이면 예외가 발생한다.")
    void createUserCoupon_WithEmptyCouponCode_ThrowsException() {
        // given & when & then
        assertThatThrownBy(() -> UserCouponModel.create(UserId.of("seyoung"), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용하지 않은 쿠폰을 사용할 수 있다.")
    void useCoupon_UnusedCoupon_Success() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when
        userCoupon.reserve(1L);
        boolean result = userCoupon.useCoupon(today, 1L);

        // then
        assertThat(result).isTrue();
        assertThat(userCoupon.getStatus()).isEqualTo(UserCoupontStatus.USED);
        assertThat(userCoupon.getUsedAt()).isEqualTo(today);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하려고 하면 예외가 발생한다.")
    void useCoupon_AlreadyUsedCoupon_ThrowsException() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");
        userCoupon.reserve(1L);
        userCoupon.useCoupon(today, 1L); // 첫 번째 사용

        // when & then
        assertThatThrownBy(() -> userCoupon.useCoupon(tomorrow, 2L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("사용하지 않은 쿠폰의 status는 AVAILABLE 반환한다.")
    void isUsed_UnusedCoupon_ReturnsFalse() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        assertThat(userCoupon.getStatus()).isEqualTo(UserCoupontStatus.AVAILABLE);
    }

    @Test
    @DisplayName("사용된 쿠폰의 status USED를 반환한다.")
    void isUsed_UsedCoupon_ReturnsTrue() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");
        userCoupon.reserve(1L);
        userCoupon.useCoupon(today, 1L);

        // when & then
        assertThat(userCoupon.getStatus()).isEqualTo(UserCoupontStatus.USED);
    }

    @Test
    @DisplayName("사용하지 않은 쿠폰의 usedAt은 null이다.")
    void getUsedAt_UnusedCoupon_ReturnsNull() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when & then
        assertThat(userCoupon.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("사용된 쿠폰의 usedAt은 사용 날짜를 반환한다.")
    void getUsedAt_UsedCoupon_ReturnsUsedDate() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");
        userCoupon.reserve(1L);
        userCoupon.useCoupon(today, 1L);

        // when & then
        assertThat(userCoupon.getUsedAt()).isEqualTo(today);
    }

    @Test
    @DisplayName("여러 사용자 쿠폰을 생성할 수 있다.")
    void createMultipleUserCoupons_Success() {
        // given & when
        UserCouponModel userCoupon1 = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");
        UserCouponModel userCoupon2 = UserCouponModel.create(UserId.of("seyoung12"), "COUPON456");
        UserCouponModel userCoupon3 = UserCouponModel.create(UserId.of("seyoung"), "COUPON789");

        // then
        assertThat(userCoupon1.getUserId()).isEqualTo(UserId.of("seyoung"));
        assertThat(userCoupon1.getCouponCode()).isEqualTo("COUPON123");
        assertThat(userCoupon1.getStatus()).isEqualTo(UserCoupontStatus.AVAILABLE);

        assertThat(userCoupon2.getUserId()).isEqualTo(UserId.of("seyoung12"));
        assertThat(userCoupon2.getCouponCode()).isEqualTo("COUPON456");
        assertThat(userCoupon2.getStatus()).isEqualTo(UserCoupontStatus.AVAILABLE);

        assertThat(userCoupon3.getUserId()).isEqualTo(UserId.of("seyoung"));
        assertThat(userCoupon3.getCouponCode()).isEqualTo("COUPON789");
        assertThat(userCoupon3.getStatus()).isEqualTo(UserCoupontStatus.AVAILABLE);
    }

    @Test
    @DisplayName("같은 사용자가 여러 쿠폰을 사용할 수 있다.")
    void useMultipleCoupons_SameUser_Success() {
        // given
        UserCouponModel userCoupon1 = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");
        UserCouponModel userCoupon2 = UserCouponModel.create(UserId.of("seyoung"), "COUPON456");

        // when
        userCoupon1.reserve(1L);
        boolean result1 = userCoupon1.useCoupon(today, 1L);
        userCoupon2.reserve(2L);
        boolean result2 = userCoupon2.useCoupon(tomorrow, 2L);

        // then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(userCoupon1.getStatus()).isEqualTo(UserCoupontStatus.USED);
        assertThat(userCoupon2.getStatus()).isEqualTo(UserCoupontStatus.USED);
        assertThat(userCoupon1.getUsedAt()).isEqualTo(today);
        assertThat(userCoupon2.getUsedAt()).isEqualTo(tomorrow);
    }

    @Test
    @DisplayName("다른 날짜에 쿠폰을 사용할 수 있다.")
    void useCoupon_DifferentDates_Success() {
        // given
        UserCouponModel userCoupon = UserCouponModel.create(UserId.of("seyoung"), "COUPON123");

        // when
        userCoupon.reserve(1L);
        boolean result = userCoupon.useCoupon(tomorrow, 1L);

        // then
        assertThat(result).isTrue();
        assertThat(userCoupon.getStatus()).isEqualTo(UserCoupontStatus.USED);
        assertThat(userCoupon.getUsedAt()).isEqualTo(tomorrow);
    }
}
