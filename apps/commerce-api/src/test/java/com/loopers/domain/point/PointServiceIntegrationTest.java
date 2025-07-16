package com.loopers.domain.point;

import com.loopers.domain.user.*;
import com.loopers.support.error.CoreException;
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
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("포인트 충전 테스트")
    @Nested
    class ChargePoint {

        @DisplayName("포인트 충전 성공")
        @Test
        void chargeMyPointSuccess() {
            // given
            UserModel user = userRepository.create(UserModel.builder()
                    .userId(new UserId("seyoung"))
                    .email(new Email("seyoung@loopers.com"))
                    .gender(Gender.MALE)
                    .birthDate(new BirthDate("2000-01-01"))
                    .build());

            int amount = 1000;

            // when
            PointModel pointModel = pointService.chargeMyPoint(user.getUserId().getValue(), amount);

            // then
            assertThat(pointModel.getAmount()).isEqualTo(amount);
        }

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void chargeMyPointFailureInvalidUserId() {
            // given
            String userId = "invalidUser";
            int amount = 1000;

            // when & then
            assertThatThrownBy(() -> pointService.chargeMyPoint(userId, amount))
                    .isInstanceOf(CoreException.class);
        }
    }


}
