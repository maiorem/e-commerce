package com.loopers.application.point;

import com.loopers.domain.user.*;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class PointApplicationServiceIntegrationTest {

    @Autowired
    private PointApplicationService pointApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 충전 테스트")
    @Nested
    class ChargePoint {

        @DisplayName("Header로 회원 정보, Body로 충전할 포인트가 주어지면 포인트 충전에 성공한다")
        @Test
        void chargeMyPointSuccess() {
            // given
            UserModel user = userRepository.save(UserModel.of(
                    UserId.of("seyoung"),
                    Email.of("seyoung@loopers.com"),
                    Gender.MALE,
                    BirthDate.of("2000-01-01")
            ));

            int amount = 1000;

            // when
            PointInfo pointInfo = pointApplicationService.chargeMyPoint(user.getUserId().getValue(), amount);

            // then
            assertThat(pointInfo.totalAmount()).isEqualTo(amount);
        }

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void chargeMyPointFailureInvalidUserId() {
            // given
            String userId = "invalidUser";
            int amount = 1000;

            // when & then
            assertThatThrownBy(() -> pointApplicationService.chargeMyPoint(userId, amount))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @DisplayName("포인트 조회 테스트")
    @Nested
    class GetPoint {

        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void getMyPointSuccess() {
            // given
            UserModel user = userRepository.save(UserModel.of(
                    UserId.of("seyoung"),
                    Email.of("seyoung@loopers.com"),
                    Gender.MALE,
                    BirthDate.of("2000-01-01")
            ));
            int initialAmount = 500;
            pointApplicationService.chargeMyPoint(user.getUserId().getValue(), initialAmount);

            // when
            PointInfo info = pointApplicationService.getMyPoint(user.getUserId().getValue());

            // then
            assertThat(info.userId()).isEqualTo(user.getUserId().getValue());
            assertThat(info.totalAmount()).isEqualTo(initialAmount);

        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void getMyPointFailureInvalidUserId() {
            // given
            String userId = "seyoung123";

            // when
            PointInfo info = pointApplicationService.getMyPoint(userId);

            // then
            assertThat(info).isNull();
        }
    }


} 
