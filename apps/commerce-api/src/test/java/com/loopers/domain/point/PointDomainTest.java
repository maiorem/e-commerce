package com.loopers.domain.point;

import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PointDomainTest {

    @InjectMocks
    private PointDomainService pointDomainService;

    @Nested
    @DisplayName("PointModel 생성")
    class CreatePointModel {

        @Test
        @DisplayName("유효한 파라미터로 PointModel을 생성하면 성공한다.")
        void createPointModel_withValidParameters_success() {
            // given
            UserId userId = UserId.of("testuser");
            int amount = 1000;

            // when
            PointModel point = PointModel.of(userId, amount);

            // then
            assertThat(point.getUserId()).isEqualTo(userId);
            assertThat(point.getAmount()).isEqualTo(amount);
            assertThat(point.getExpiredAt()).isAfter(LocalDateTime.now());
        }

        @Test
        @DisplayName("유효한 파라미터와 만료일로 PointModel을 생성하면 성공한다.")
        void createPointModel_withValidParametersAndExpiredAt_success() {
            // given
            UserId userId = UserId.of("testuser");
            int amount = 1000;
            LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

            // when
            PointModel point = PointModel.of(userId, amount, expiredAt);

            // then
            assertThat(point.getUserId()).isEqualTo(userId);
            assertThat(point.getAmount()).isEqualTo(amount);
            assertThat(point.getExpiredAt()).isEqualTo(expiredAt);
        }

        @Test
        @DisplayName("null UserId로 PointModel을 생성하면 예외가 발생한다.")
        void createPointModel_withNullUserId_throwsException() {
            // when & then
            assertThatThrownBy(() -> PointModel.of(null, 1000))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 포인트로 PointModel을 생성하면 예외가 발생한다.")
        void createPointModel_withNegativeAmount_throwsException() {
            // given
            UserId userId = UserId.of("testuser");

            // when & then
            assertThatThrownBy(() -> PointModel.of(userId, -100))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("null 만료일로 PointModel을 생성하면 예외가 발생한다.")
        void createPointModel_withNullExpiredAt_throwsException() {
            // given
            UserId userId = UserId.of("testuser");

            // when & then
            assertThatThrownBy(() -> PointModel.of(userId, 1000, null))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("포인트 충전 시,")
    class ChargePoint {

        @Test
        @DisplayName("유효한 금액으로 포인트를 충전하면 성공한다.")
        void chargePoint_withValidAmount_success() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when
            int result = point.charge(500);

            // then
            assertThat(result).isEqualTo(1500);
            assertThat(point.getAmount()).isEqualTo(1500);
        }

        @Test
        @DisplayName("0 이하의 금액으로 포인트를 충전하면 예외가 발생한다.")
        void chargePoint_withInvalidAmount_throwsException() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when & then
            assertThatThrownBy(() -> point.charge(0))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);

            assertThatThrownBy(() -> point.charge(-100))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("포인트 사용 시,")
    class UsePoint {

        @Test
        @DisplayName("유효한 금액으로 포인트를 사용하면 성공한다.")
        void usePoint_withValidAmount_success() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when
            int result = point.use(500);

            // then
            assertThat(result).isEqualTo(500);
            assertThat(point.getAmount()).isEqualTo(500);
        }

        @Test
        @DisplayName("0 이하의 금액으로 포인트를 사용하면 예외가 발생한다.")
        void usePoint_withInvalidAmount_throwsException() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when & then
            assertThatThrownBy(() -> point.use(0))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);

            assertThatThrownBy(() -> point.use(-100))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("잔액보다 많은 금액으로 포인트를 사용하면 예외가 발생한다.")
        void usePoint_withInsufficientBalance_throwsException() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when & then
            assertThatThrownBy(() -> point.use(1500))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("포인트 만료 확인 시,")
    class CheckExpiration {

        @Test
        @DisplayName("만료되지 않은 포인트는 false를 반환한다.")
        void isExpired_withNotExpiredPoint_returnsFalse() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when
            boolean result = point.isExpired();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("만료된 포인트는 true를 반환한다.")
        void isExpired_withExpiredPoint_returnsTrue() {
            // given
            PointModel point = PointModel.of(
                    UserId.of("testuser"), 
                    1000, 
                    LocalDateTime.now().minusDays(1)
            );

            // when
            boolean result = point.isExpired();

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 충전")
    class DomainServiceChargePoint {

        @Test
        @DisplayName("유효한 파라미터로 포인트를 충전하면 성공한다.")
        void chargePoint_withValidParameters_success() {
            // given
            UserId userId = UserId.of("testuser");
            int amount = 1000;

            // when
            PointModel result = pointDomainService.chargePoint(userId, amount);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getAmount()).isEqualTo(amount);
        }

        @Test
        @DisplayName("기존 포인트에 유효한 금액을 충전하면 성공한다.")
        void chargePoint_withExistingPoint_success() {
            // given
            PointModel existingPoint = PointModel.of(UserId.of("testuser"), 1000);
            int amount = 500;

            // when
            PointModel result = pointDomainService.chargePoint(existingPoint, amount);

            // then
            assertThat(result.getAmount()).isEqualTo(1500);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -100})
        @DisplayName("유효하지 않은 충전 금액이 주어지면 예외가 발생한다.")
        void chargePoint_withInvalidAmount_throwsException(int invalidAmount) {
            // when & then
            assertThatThrownBy(() -> pointDomainService.chargePoint(UserId.of("testuser"), invalidAmount))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("100만원을 초과하는 금액으로 충전하면 예외가 발생한다.")
        void chargePoint_withExcessiveAmount_throwsException() {
            // when & then
            assertThatThrownBy(() -> pointDomainService.chargePoint(UserId.of("testuser"), 1000001))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 사용")
    class DomainServiceUsePoint {

        @Test
        @DisplayName("유효한 금액으로 포인트를 사용하면 성공한다.")
        void usePoint_withValidAmount_success() {
            // given
            PointModel existingPoint = PointModel.of(UserId.of("testuser"), 1000);
            int amount = 500;

            // when
            PointModel result = pointDomainService.usePoint(existingPoint, amount);

            // then
            assertThat(result.getAmount()).isEqualTo(500);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -100})
        @DisplayName("유효하지 않은 사용 금액이 주어지면 예외가 발생한다.")
        void usePoint_withInvalidAmount_throwsException(int invalidAmount) {
            // given
            PointModel existingPoint = PointModel.of(UserId.of("testuser"), 1000);

            // when & then
            assertThatThrownBy(() -> pointDomainService.usePoint(existingPoint, invalidAmount))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("잔액보다 많은 금액으로 포인트를 사용하면 예외가 발생한다.")
        void usePoint_withInsufficientBalance_throwsException() {
            // given
            PointModel existingPoint = PointModel.of(UserId.of("testuser"), 1000);
            int amount = 1500;

            // when & then
            assertThatThrownBy(() -> pointDomainService.usePoint(existingPoint, amount))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 환불")
    class DomainServiceRefundPoint {

        @Test
        @DisplayName("유효한 금액으로 포인트를 환불하면 성공한다.")
        void refundPoint_withValidAmount_success() {
            // given
            PointModel existingPoint = PointModel.of(UserId.of("testuser"), 1000);
            int amount = 500;

            // when
            PointModel result = pointDomainService.refundPoint(existingPoint, amount);

            // then
            assertThat(result.getAmount()).isEqualTo(1500);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -100})
        @DisplayName("유효하지 않은 환불 금액이 주어지면 예외가 발생한다.")
        void refundPoint_withInvalidAmount_throwsException(int invalidAmount) {
            // given
            PointModel existingPoint = PointModel.of(UserId.of("testuser"), 1000);

            // when & then
            assertThatThrownBy(() -> pointDomainService.refundPoint(existingPoint, invalidAmount))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 만료 확인")
    class DomainServiceCheckExpiration {

        @Test
        @DisplayName("만료되지 않은 포인트는 false를 반환한다.")
        void isPointExpired_withNotExpiredPoint_returnsFalse() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when
            boolean result = pointDomainService.isPointExpired(point);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("만료된 포인트는 true를 반환한다.")
        void isPointExpired_withExpiredPoint_returnsTrue() {
            // given
            PointModel point = PointModel.of(
                    UserId.of("testuser"), 
                    1000, 
                    LocalDateTime.now().minusDays(1)
            );

            // when
            boolean result = pointDomainService.isPointExpired(point);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("null 포인트는 false를 반환한다.")
        void isPointExpired_withNullPoint_returnsFalse() {
            // when
            boolean result = pointDomainService.isPointExpired(null);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 만료 처리")
    class DomainServiceExpirePoint {

        @Test
        @DisplayName("만료되지 않은 포인트는 그대로 반환한다.")
        void expirePoint_withNotExpiredPoint_returnsSame() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);

            // when
            PointModel result = pointDomainService.expirePoint(point);

            // then
            assertThat(result).isEqualTo(point);
        }

        @Test
        @DisplayName("만료된 포인트는 0으로 설정하여 반환한다.")
        void expirePoint_withExpiredPoint_returnsZeroAmount() {
            // given
            PointModel point = PointModel.of(
                    UserId.of("testuser"), 
                    1000, 
                    LocalDateTime.now().minusDays(1)
            );

            // when
            PointModel result = pointDomainService.expirePoint(point);

            // then
            assertThat(result.getAmount()).isEqualTo(0);
            assertThat(result.getUserId()).isEqualTo(point.getUserId());
        }

        @Test
        @DisplayName("null 포인트로 만료 처리를 시도하면 예외가 발생한다.")
        void expirePoint_withNullPoint_throwsException() {
            // when & then
            assertThatThrownBy(() -> pointDomainService.expirePoint(null))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 내역 생성")
    class DomainServiceCreatePointHistory {

        @Test
        @DisplayName("유효한 파라미터로 포인트 내역을 생성하면 성공한다.")
        void createPointHistory_withValidParameters_success() {
            // given
            UserId userId = UserId.of("testuser");
            int changedAmount = 1000;
            int currentAmount = 1500;
            PointChangeReason reason = PointChangeReason.PROMOTION;

            // when
            PointHistoryModel result = pointDomainService.createPointHistory(userId, changedAmount, currentAmount, reason);

            // then
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getChangedAmount()).isEqualTo(changedAmount);
            assertThat(result.getCurrentAmount()).isEqualTo(currentAmount);
            assertThat(result.getReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("null UserId로 포인트 내역을 생성하면 예외가 발생한다.")
        void createPointHistory_withNullUserId_throwsException() {
            // when & then
            assertThatThrownBy(() -> pointDomainService.createPointHistory(null, 1000, 1500, PointChangeReason.PROMOTION))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("null 이유로 포인트 내역을 생성하면 예외가 발생한다.")
        void createPointHistory_withNullReason_throwsException() {
            // when & then
            assertThatThrownBy(() -> pointDomainService.createPointHistory(UserId.of("testuser"), 1000, 1500, null))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 잔액으로 포인트 내역을 생성하면 예외가 발생한다.")
        void createPointHistory_withNegativeCurrentAmount_throwsException() {
            // when & then
            assertThatThrownBy(() -> pointDomainService.createPointHistory(UserId.of("testuser"), 1000, -100, PointChangeReason.PROMOTION))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("PointDomainService 포인트 잔액 확인")
    class DomainServiceCheckSufficientPoint {

        @Test
        @DisplayName("충분한 포인트가 있으면 true를 반환한다.")
        void hasSufficientPoint_withSufficientPoint_returnsTrue() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);
            int requiredAmount = 500;

            // when
            boolean result = pointDomainService.hasSufficientPoint(point, requiredAmount);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("부족한 포인트가 있으면 false를 반환한다.")
        void hasSufficientPoint_withInsufficientPoint_returnsFalse() {
            // given
            PointModel point = PointModel.of(UserId.of("testuser"), 1000);
            int requiredAmount = 1500;

            // when
            boolean result = pointDomainService.hasSufficientPoint(point, requiredAmount);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null 포인트는 false를 반환한다.")
        void hasSufficientPoint_withNullPoint_returnsFalse() {
            // when
            boolean result = pointDomainService.hasSufficientPoint(null, 500);

            // then
            assertThat(result).isFalse();
        }
    }
}
