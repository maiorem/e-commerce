package com.loopers.application.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.config.TestConfig;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@SpringBootTest
@Import(TestConfig.class)
class UserApplicationServiceIntegrationTest {

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        // spy 객체의 호출 기록 리셋
        reset(userRepository);
    }

    @DisplayName("회원가입 시,")
    @Nested
    class CreateUser {

        @DisplayName("유효한 회원 정보가 주어지면, 회원가입에 성공하고 DB에 저장 요청을 보낸다.")
        @Test
        void createUser_withValidInfo_success() {
            // given
            String userId = "seyoung";
            String email = "seyoung@loopers.com";
            String gender = "FEMALE";
            String birthDate = "2000-01-01";
            ArgumentCaptor<UserModel> userModelArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);

            // when
            UserInfo userInfo = userApplicationService.createUser(userId, email, gender, birthDate);

            // then
            assertAll(
                    () -> assertThat(userInfo).isNotNull(),
                    () -> assertThat(userInfo.userId()).isEqualTo(userId),
                    () -> assertThat(userInfo.email()).isEqualTo(email),
                    () -> assertThat(userInfo.gender()).isEqualTo(gender),
                    () -> assertThat(userInfo.birthDate()).isEqualTo(birthDate)
            );
            assertThat(userRepository.existsByUserId(UserId.of(userId))).isTrue();

            // 행위 검증
            verify(userRepository, times(1)).save(userModelArgumentCaptor.capture());
            UserModel capturedUser = userModelArgumentCaptor.getValue();
            assertThat(capturedUser.getUserId().getValue()).isEqualTo(userId);
            assertThat(capturedUser.getEmail().getValue()).isEqualTo(email);
        }

        @DisplayName("이미 존재하는 아이디로 회원가입을 시도하면, 예외가 발생한다.")
        @Test
        void createUser_withExistingUserId_throwsException() {

            // given
            String userId = "seyoung";
            String email = "seyoung@loopers.com";
            String gender = "FEMALE";
            String birthDate = "2000-01-01";

            // 첫 번째 회원가입 (성공)
            userApplicationService.createUser(userId, email, gender, birthDate);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () ->
                    userApplicationService.createUser(userId, "new@loopers.com", String.valueOf(Gender.MALE), "2001-01-01")
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }


    @DisplayName("내 정보 조회 시")
    @Nested
    class GetMyInfo {

        @DisplayName("존재하는 유저의 아이디가 주어지면, 해당 유저의 정보를 조회한다.")
        @Test
        void getMyInfo_withExistingUserId_success() {
            // given
            String userId = "seyoung";
            String email = "seyoung@loopers.com";
            String gender = "FEMALE";
            String birthDate = "2000-01-01";

            // 회원가입
            userApplicationService.createUser(userId, email, gender, birthDate);

            //when
            UserInfo userInfo = userApplicationService.getUser(userId);

            // then
            assertAll(
                () -> assertThat(userInfo).isNotNull(),
                () -> assertThat(userInfo.userId()).isEqualTo(userId),
                () -> assertThat(userInfo.email()).isEqualTo(email)
            );
        }

        @DisplayName("존재하지 않는 유저의 아이디가 주어지면 예외가 발생한다.")
        @Test
        void getMyInfo_withNonExistingUserId_null() {
            // given
            String userId = "seyoung1";

            // when & then
            assertThrows(CoreException.class, () -> userApplicationService.getUser(userId));
        }
    
    }
} 
