package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponProcessorTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserCouponRepository userCouponRepository;

    @Mock
    private CouponValidationDomainService couponValidationDomainService;

    @InjectMocks
    private CouponProcessor couponProcessor;

    private UserId userId;
    private String couponCode;
    private CouponModel coupon;
    private UserCouponModel userCoupon;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        userId = UserId.of("testuser");
        couponCode = "TEST-COUPON-123";
        today = LocalDate.now();

        // 정액 할인 쿠폰 생성
        coupon = CouponModel.builder()
                .name("테스트 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(5000)
                .minimumOrderAmount(10000)
                .maximumDiscountAmount(5000)
                .issuedAt(today)
                .validUntil(today.plusDays(30))
                .build();

        // 사용자 쿠폰 생성 (발급일은 리플렉션 사용)
        userCoupon = UserCouponModel.create(userId, couponCode);
        try {
            Field issuedAtField = UserCouponModel.class.getDeclaredField("issuedAt");
            issuedAtField.setAccessible(true);
            issuedAtField.set(userCoupon, today);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set issuedAt field", e);
        }
    }

    @Test
    @DisplayName("쿠폰 코드가 null이면 원래 주문 금액을 반환한다.")
    void applyCouponDiscount_WithNullCouponCode_ReturnsOriginalPrice() {
        // given
        int orderPrice = 15000;

        // when
        int result = couponProcessor.applyCouponDiscount(userId, orderPrice, null);

        // then
        assertThat(result).isEqualTo(orderPrice);
        verifyNoInteractions(couponRepository, userCouponRepository, couponValidationDomainService);
    }

    @Test
    @DisplayName("쿠폰 코드가 빈 문자열이면 원래 주문 금액을 반환한다.")
    void applyCouponDiscount_WithEmptyCouponCode_ReturnsOriginalPrice() {
        // given
        int orderPrice = 15000;

        // when
        int result = couponProcessor.applyCouponDiscount(userId, orderPrice, "");

        // then
        assertThat(result).isEqualTo(orderPrice);
        verifyNoInteractions(couponRepository, userCouponRepository, couponValidationDomainService);
    }

    @Test
    @DisplayName("유효한 쿠폰을 적용하면 할인된 금액을 반환한다.")
    void applyCouponDiscount_WithValidCoupon_ReturnsDiscountedPrice() {
        // given
        int orderPrice = 15000;
        when(couponRepository.findByCouponCode(couponCode)).thenReturn(Optional.of(coupon));
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.of(userCoupon));
        doNothing().when(couponValidationDomainService).validateCouponUsage(any(), any(), anyInt(), any());

        // when
        int result = couponProcessor.applyCouponDiscount(userId, orderPrice, couponCode);

        // then
        assertThat(result).isEqualTo(10000); // 15000 - 5000 = 10000
        verify(couponRepository).findByCouponCode(couponCode);
        verify(userCouponRepository).findByUserIdAndCouponCode(userId, couponCode);
        verify(couponValidationDomainService).validateCouponUsage(userCoupon, coupon, orderPrice, today);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 코드를 사용하면 예외가 발생한다.")
    void applyCouponDiscount_WithInvalidCouponCode_ThrowsException() {
        // given
        int orderPrice = 15000;
        when(couponRepository.findByCouponCode(couponCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponProcessor.applyCouponDiscount(userId, orderPrice, couponCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("사용자가 보유하지 않은 쿠폰을 사용하면 예외가 발생한다.")
    void applyCouponDiscount_WithUserNotHavingCoupon_ThrowsException() {
        // given
        int orderPrice = 15000;
        when(couponRepository.findByCouponCode(couponCode)).thenReturn(Optional.of(coupon));
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponProcessor.applyCouponDiscount(userId, orderPrice, couponCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND)
                .hasMessage("사용자에게 해당 쿠폰이 없습니다.");
    }

    @Test
    @DisplayName("쿠폰 할인 금액이 주문 총액을 초과하면 예외가 발생한다.")
    void applyCouponDiscount_WithDiscountExceedingOrderPrice_ThrowsException() {
        // given
        // 최소 주문 금액을 충족하면서도 할인 금액이 주문 금액을 초과하는 상황 생성
        CouponModel highDiscountCoupon = CouponModel.builder()
                .name("높은 할인 쿠폰")
                .type(CouponType.FIXED_AMOUNT)
                .discountValue(8000)
                .minimumOrderAmount(5000)
                .maximumDiscountAmount(8000)
                .issuedAt(today)
                .validUntil(today.plusDays(30))
                .build();

        int orderPrice = 6000; // 주문 금액 6000원 (할인 금액 8000원보다 작음)
        when(couponRepository.findByCouponCode(couponCode)).thenReturn(Optional.of(highDiscountCoupon));
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.of(userCoupon));
        doNothing().when(couponValidationDomainService).validateCouponUsage(any(), any(), anyInt(), any());

        // when & then
        assertThatThrownBy(() -> couponProcessor.applyCouponDiscount(userId, orderPrice, couponCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("쿠폰 코드가 null이면 useCoupon은 아무것도 하지 않는다.")
    void useCoupon_WithNullCouponCode_DoesNothing() {
        // when
        couponProcessor.useCoupon(userId, null);

        // then
        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("쿠폰 코드가 빈 문자열이면 useCoupon은 아무것도 하지 않는다.")
    void useCoupon_WithEmptyCouponCode_DoesNothing() {
        // when
        couponProcessor.useCoupon(userId, "");

        // then
        verifyNoInteractions(userCouponRepository);
    }

    @Test
    @DisplayName("유효한 쿠폰을 사용하면 쿠폰이 사용 처리된다.")
    void useCoupon_WithValidCoupon_MarksCouponAsUsed() {
        // given
        UserCouponModel mockUserCoupon = mock(UserCouponModel.class);
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.of(mockUserCoupon));
        when(userCouponRepository.save(any(UserCouponModel.class))).thenReturn(mockUserCoupon);
        when(mockUserCoupon.useCoupon(any(LocalDate.class))).thenReturn(true);

        // when
        couponProcessor.useCoupon(userId, couponCode);

        // then
        verify(userCouponRepository).findByUserIdAndCouponCode(userId, couponCode);
        verify(mockUserCoupon).useCoupon(today);
        verify(userCouponRepository).save(mockUserCoupon);
    }

    @Test
    @DisplayName("사용자가 보유하지 않은 쿠폰을 사용하려 하면 예외가 발생한다.")
    void useCoupon_WithUserNotHavingCoupon_ThrowsException() {
        // given
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponProcessor.useCoupon(userId, couponCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 사용하려 하면 예외가 발생한다.")
    void useCoupon_WithAlreadyUsedCoupon_ThrowsException() {
        // given
        UserCouponModel mockUserCoupon = mock(UserCouponModel.class);
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.of(mockUserCoupon));
        doThrow(new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다."))
                .when(mockUserCoupon).useCoupon(any(LocalDate.class));

        // when & then
        assertThatThrownBy(() -> couponProcessor.useCoupon(userId, couponCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }

    @Test
    @DisplayName("쿠폰 검증 실패 시 예외가 발생한다.")
    void applyCouponDiscount_WithValidationFailure_ThrowsException() {
        // given
        int orderPrice = 15000;
        when(couponRepository.findByCouponCode(couponCode)).thenReturn(Optional.of(coupon));
        when(userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)).thenReturn(Optional.of(userCoupon));
        doThrow(new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 유효하지 않습니다."))
                .when(couponValidationDomainService).validateCouponUsage(any(), any(), anyInt(), any());

        // when & then
        assertThatThrownBy(() -> couponProcessor.applyCouponDiscount(userId, orderPrice, couponCode))
                .isInstanceOf(CoreException.class)
                .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
    }
}
