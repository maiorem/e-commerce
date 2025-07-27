package com.loopers.application.user;

import com.loopers.domain.user.*;
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
import com.loopers.support.config.TestConfig;

import java.util.UUID;

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

    private UserId createValidUserId() {
        return UserId.of(UUID.randomUUID().toString().substring(0, 8));
    }

    private Email createValidEmail() {
        return Email.of("seyoung@loopers.com");
    }

    private BirthDate createValidBirthDate() {
        return BirthDate.of("2000-01-01");
    }

    @DisplayName("회원가입 시,")
    @Nested
    class CreateUser {

        @DisplayName("유효한 회원 정보가 주어지면, 회원가입에 성공하고 DB에 저장 요청을 보낸다.")
        @Test
        void createUser_withValidInfo_success() {
            // given
            UserId userId = createValidUserId();
            Email email = createValidEmail();
            Gender gender = Gender.MALE;
            BirthDate birthDate = createValidBirthDate();
            ArgumentCaptor<UserModel> userModelArgumentCaptor = ArgumentCaptor.forClass(UserModel.class);

            // when
            UserModel userModel = userApplicationService.createUser(userId, email, gender, birthDate);

            // then
            assertAll(
                    () -> assertThat(userModel).isNotNull(),
                    () -> assertThat(userModel.getUserId()).isEqualTo(userId),
                    () -> assertThat(userModel.getEmail()).isEqualTo(email),
                    () -> assertThat(userModel.getGender()).isEqualTo(gender),
                    () -> assertThat(userModel.getBirthDate()).isEqualTo(birthDate)
            );
            assertThat(userRepository.existsByUserId(userId)).isTrue();

            // 행위 검증
            verify(userRepository, times(1)).save(userModelArgumentCaptor.capture());
            UserModel capturedUser = userModelArgumentCaptor.getValue();
            assertThat(capturedUser.getUserId()).isEqualTo(userId);
            assertThat(capturedUser.getEmail()).isEqualTo(email);
        }

        @DisplayName("이미 존재하는 아이디로 회원가입을 시도하면, 예외가 발생한다.")
        @Test
        void createUser_withExistingUserId_throwsException() {

            // given
            UserId userId = createValidUserId();
            Email email = createValidEmail();
            Gender gender = Gender.MALE;
            BirthDate birthDate = createValidBirthDate();

            // 첫 번째 회원가입 (성공)
            userApplicationService.createUser(userId, email, gender, birthDate);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () ->
                    userApplicationService.createUser(userId, Email.of("new@loopers.com"), Gender.MALE, BirthDate.of("2001-01-01"))
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
            UserId userId = createValidUserId();
            Email email = createValidEmail();
            Gender gender = Gender.MALE;
            BirthDate birthDate = createValidBirthDate();

            // 회원가입
            userApplicationService.createUser(userId, email, gender, birthDate);

            //when
            UserModel userModel = userApplicationService.getUser(userId);

            // then
            assertAll(
                () -> assertThat(userModel).isNotNull(),
                () -> assertThat(userModel.getUserId()).isEqualTo(userId),
                () -> assertThat(userModel.getEmail()).isEqualTo(email)
            );
        }

        @DisplayName("존재하지 않는 유저의 아이디가 주어지면 null이 발생한다.")
        @Test
        void getMyInfo_withNonExistingUserId_null() {
            // given
            UserId userId = createValidUserId();

            // when & then
            assertThat(userApplicationService.getUser(userId)).isNull();
        }
    
    }
} 