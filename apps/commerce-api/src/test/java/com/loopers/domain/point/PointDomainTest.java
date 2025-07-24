package com.loopers.domain.point;

import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointDomainTest {

    private UserId createValidUserId() {
        return new UserId("testuser");
    }

    @DisplayName("PointModel 객체를 생성할 때,")
    @Nested
    class Create {

        @DisplayName("유효한 userId와 초기 포인트가 주어지면 정상적으로 생성된다.")
        @Test
        void createPointModel_withValidData() {
            // given
            UserId userId = createValidUserId();
            int initialAmount = 1000;

            // when
            PointModel pointModel = new PointModel(userId, initialAmount);

            // then
            assertAll(
                () -> assertThat(pointModel.getUserId()).isEqualTo(userId),
                () -> assertThat(pointModel.getAmount()).isEqualTo(initialAmount)
            );
        }

        @DisplayName("userId가 null이면 예외가 발생한다.")
        @Test
        void createPointModel_withNullUserId_throwsException() {
            // given
            UserId userId = null;
            int initialAmount = 1000;

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> new PointModel(userId, initialAmount));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("초기 포인트가 음수이면 예외가 발생한다.")
        @Test
        void createPointModel_withNegativeInitialAmount_throwsException() {
            // given
            UserId userId = createValidUserId();
            int initialAmount = -100;

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> new PointModel(userId, initialAmount));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("포인트를 추가할 때,")
    @Nested
    class AddPoint {

        @DisplayName("양수 포인트를 추가하면 잔여 포인트가 증가한다.")
        @Test
        void addPositivePoint_increasesAmount() {
            // given
            PointModel pointModel = new PointModel(createValidUserId(), 1000);
            int pointToAdd = 500;

            // when
            int newAmount = pointModel.addPoint(pointToAdd);

            // then
            assertThat(newAmount).isEqualTo(1500);
            assertThat(pointModel.getAmount()).isEqualTo(1500);
        }

        @DisplayName("0 또는 음수 포인트를 추가하려 하면 예외가 발생한다.")
        @Test
        void addZeroOrNegativePoint_throwsException() {
            // given
            PointModel pointModel = new PointModel(createValidUserId(), 1000);

            // when & then
            assertAll(
                () -> {
                    CoreException exception = assertThrows(CoreException.class, () -> pointModel.addPoint(0));
                    assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                },
                () -> {
                    CoreException exception = assertThrows(CoreException.class, () -> pointModel.addPoint(-100));
                    assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                }
            );
        }
    }

    @DisplayName("포인트를 차감할 때,")
    @Nested
    class RemovePoint {

        @DisplayName("양수 포인트를 차감하면 잔여 포인트가 감소한다.")
        @Test
        void removePositivePoint_decreasesAmount() {
            // given
            PointModel pointModel = new PointModel(createValidUserId(), 1000);
            int pointToRemove = 300;

            // when
            int newAmount = pointModel.removePoint(pointToRemove);

            // then
            assertThat(newAmount).isEqualTo(700);
            assertThat(pointModel.getAmount()).isEqualTo(700);
        }

        @DisplayName("잔여 포인트보다 많은 포인트를 차감하려 하면 예외가 발생한다.")
        @Test
        void removeMoreThanCurrentAmount_throwsException() {
            // given
            PointModel pointModel = new PointModel(createValidUserId(), 500);
            int pointToRemove = 1000;

            // when & then
            CoreException exception = assertThrows(CoreException.class, () -> pointModel.removePoint(pointToRemove));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("0 또는 음수 포인트를 차감하려 하면 예외가 발생한다.")
        @Test
        void removeZeroOrNegativePoint_throwsException() {
            // given
            PointModel pointModel = new PointModel(createValidUserId(), 1000);

            // when & then
            assertAll(
                () -> {
                    CoreException exception = assertThrows(CoreException.class, () -> pointModel.removePoint(0));
                    assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                },
                () -> {
                    CoreException exception = assertThrows(CoreException.class, () -> pointModel.removePoint(-100));
                    assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
                }
            );
        }
    }
}
