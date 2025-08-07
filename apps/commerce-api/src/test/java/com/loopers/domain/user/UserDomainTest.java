package com.loopers.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserDomainTest {

    @InjectMocks
    private UserDomainService userDomainService;

    @Nested
    @DisplayName("UserId 생성 시,")
    class Create_UserId {

        @Nested
        @DisplayName("유효한 사용자 ID로 요청할 경우")
        class Valid_UserId_Request {

            @DisplayName("4~10자 영문 및 숫자로 구성된 사용자 ID가 생성된다.")
            @ParameterizedTest
            @ValueSource(strings = {"user", "user123", "user12345", "user123456"})
            void createUserId_withValidUserId_success(String validUserId) {
                // when
                UserId userId = UserId.of(validUserId);

                // then
                assertThat(userId.getValue()).isEqualTo(validUserId);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 사용자 ID로 요청할 경우")
        class Invalid_UserId_Request {

            @DisplayName("3자 이하의 사용자 ID로 요청 시 예외가 발생한다.")
            @ParameterizedTest
            @ValueSource(strings = {"", "a", "ab", "abc"})
            void createUserId_withTooShortUserId_throwsException(String invalidUserId) {
                // when & then
                assertThatThrownBy(() -> UserId.of(invalidUserId))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("11자 이상의 사용자 ID로 요청 시 예외가 발생한다.")
            @ParameterizedTest
            @ValueSource(strings = {"user12345678", "user123456789"})
            void createUserId_withTooLongUserId_throwsException(String invalidUserId) {
                // when & then
                assertThatThrownBy(() -> UserId.of(invalidUserId))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("특수문자가 포함된 사용자 ID로 요청 시 예외가 발생한다.")
            @ParameterizedTest
            @ValueSource(strings = {"user@123", "user-123", "user_123", "user#123"})
            void createUserId_withSpecialCharacters_throwsException(String invalidUserId) {
                // when & then
                assertThatThrownBy(() -> UserId.of(invalidUserId))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("Email 생성 시,")
    class Create_Email {

        @Nested
        @DisplayName("유효한 이메일로 요청할 경우")
        class Valid_Email_Request {

            @DisplayName("올바른 형식의 이메일이 생성된다.")
            @ParameterizedTest
            @ValueSource(strings = {"test@example.com", "user@domain.co.kr", "user123@test.org"})
            void createEmail_withValidEmail_success(String validEmail) {
                // when
                Email email = Email.of(validEmail);

                // then
                assertThat(email.getValue()).isEqualTo(validEmail);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 이메일로 요청할 경우")
        class Invalid_Email_Request {

            @DisplayName("이메일 형식이 올바르지 않은 경우 예외가 발생한다.")
            @ParameterizedTest
            @ValueSource(strings = {"seyoung", "seyoung@", "@loopers.com"})
            void createEmail_withInvalidEmail_throwsException(String invalidEmail) {
                // when & then
                assertThatThrownBy(() -> Email.of(invalidEmail))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("빈 이메일로 요청 시 예외가 발생한다.")
            @Test
            void createEmail_withEmptyEmail_throwsException() {
                // when & then
                assertThatThrownBy(() -> Email.of(""))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("null 이메일로 요청 시 예외가 발생한다.")
            @Test
            void createEmail_withNullEmail_throwsException() {
                // when & then
                assertThatThrownBy(() -> Email.of(null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("BirthDate 생성 시,")
    class Create_BirthDate {

        @Nested
        @DisplayName("유효한 생년월일로 요청할 경우")
        class Valid_BirthDate_Request {

            @DisplayName("올바른 형식의 생년월일이 생성된다.")
            @ParameterizedTest
            @ValueSource(strings = {"1990-01-01", "2000-12-31", "1985-06-15"})
            void createBirthDate_withValidBirthDate_success(String validBirthDate) {
                // when
                BirthDate birthDate = BirthDate.of(validBirthDate);

                // then
                assertThat(birthDate.getValue().toString()).isEqualTo(validBirthDate);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 생년월일로 요청할 경우")
        class Invalid_BirthDate_Request {

            @DisplayName("미래 날짜로 요청 시 예외가 발생한다.")
            @Test
            void createBirthDate_withFutureDate_throwsException() {
                // given
                String futureDate = "2030-01-01";

                // when & then
                assertThatThrownBy(() -> BirthDate.of(futureDate))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("잘못된 날짜 형식으로 요청 시 예외가 발생한다.")
            @ParameterizedTest
            @ValueSource(strings = {"1990/01/01", "1990.01.01", "1990-13-01", "1990-01-32"})
            void createBirthDate_withInvalidFormat_throwsException(String invalidBirthDate) {
                // when & then
                assertThatThrownBy(() -> BirthDate.of(invalidBirthDate))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("빈 생년월일로 요청 시 예외가 발생한다.")
            @Test
            void createBirthDate_withEmptyBirthDate_throwsException() {
                // when & then
                assertThatThrownBy(() -> BirthDate.of(""))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("null 생년월일로 요청 시 예외가 발생한다.")
            @Test
            void createBirthDate_withNullBirthDate_throwsException() {
                // when & then
                assertThatThrownBy(() -> BirthDate.of(null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("UserModel 생성 시,")
    class Create_User {

        @Nested
        @DisplayName("유효한 파라미터로 요청할 경우")
        class Valid_Parameters_Request {

            @DisplayName("올바른 사용자 정보로 UserModel이 생성된다.")
            @Test
            void createUserModel_withValidParameters_success() {
                // given
                UserId userId = UserId.of("testuser");
                Email email = Email.of("test@example.com");
                Gender gender = Gender.MALE;
                BirthDate birthDate = BirthDate.of("1990-01-01");

                // when
                UserModel user = UserModel.of(userId, email, gender, birthDate);

                // then
                assertThat(user.getUserId()).isEqualTo(userId);
                assertThat(user.getEmail()).isEqualTo(email);
                assertThat(user.getGender()).isEqualTo(gender);
                assertThat(user.getBirthDate()).isEqualTo(birthDate);
            }
        }
    }

    @Nested
    @DisplayName("UserDomainService 사용자 생성 시,")
    class UserDomainService_Create_User {

        @Nested
        @DisplayName("유효한 파라미터로 요청할 경우")
        class Valid_Parameters_Request {

            @DisplayName("올바른 사용자 정보로 사용자가 생성된다.")
            @Test
            void createUser_withValidParameters_success() {
                // given
                UserId userId = UserId.of("testuser");
                Email email = Email.of("test@example.com");
                Gender gender = Gender.MALE;
                BirthDate birthDate = BirthDate.of("1990-01-01");

                // when
                UserModel user = userDomainService.createUser(userId, email, gender, birthDate);

                // then
                assertThat(user.getUserId()).isEqualTo(userId);
                assertThat(user.getEmail()).isEqualTo(email);
                assertThat(user.getGender()).isEqualTo(gender);
                assertThat(user.getBirthDate()).isEqualTo(birthDate);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 파라미터로 요청할 경우")
        class Invalid_Parameters_Request {

            @DisplayName("null UserId로 요청 시 예외가 발생한다.")
            @Test
            void createUser_withNullUserId_throwsException() {
                // given
                Email email = Email.of("test@example.com");
                Gender gender = Gender.MALE;
                BirthDate birthDate = BirthDate.of("1990-01-01");

                // when & then
                assertThatThrownBy(() -> userDomainService.createUser(null, email, gender, birthDate))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("null Email로 요청 시 예외가 발생한다.")
            @Test
            void createUser_withNullEmail_throwsException() {
                // given
                UserId userId = UserId.of("testuser");
                Gender gender = Gender.MALE;
                BirthDate birthDate = BirthDate.of("1990-01-01");

                // when & then
                assertThatThrownBy(() -> userDomainService.createUser(userId, null, gender, birthDate))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("null BirthDate로 요청 시 예외가 발생한다.")
            @Test
            void createUser_withNullBirthDate_throwsException() {
                // given
                UserId userId = UserId.of("testuser");
                Email email = Email.of("test@example.com");
                Gender gender = Gender.MALE;

                // when & then
                assertThatThrownBy(() -> userDomainService.createUser(userId, email, gender, null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Nested
    @DisplayName("UserDomainService 사용자 정보 수정 시,")
    class UserDomainService_Update_UserInfo {

        @Nested
        @DisplayName("유효한 파라미터로 요청할 경우")
        class Valid_Parameters_Request {

            @DisplayName("올바른 사용자 정보로 사용자 정보가 수정된다.")
            @Test
            void updateUserInfo_withValidParameters_success() {
                // given
                UserModel existingUser = UserModel.of(
                        UserId.of("testuser"),
                        Email.of("old@example.com"),
                        Gender.MALE,
                        BirthDate.of("1990-01-01")
                );
                Email newEmail = Email.of("new@example.com");
                Gender newGender = Gender.FEMALE;
                BirthDate newBirthDate = BirthDate.of("1995-01-01");

                // when
                UserModel updatedUser = userDomainService.updateUserInfo(existingUser, newEmail, newGender, newBirthDate);

                // then
                assertThat(updatedUser.getUserId()).isEqualTo(existingUser.getUserId());
                assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
                assertThat(updatedUser.getGender()).isEqualTo(newGender);
                assertThat(updatedUser.getBirthDate()).isEqualTo(newBirthDate);
            }
        }

        @Nested
        @DisplayName("유효하지 않은 파라미터로 요청할 경우")
        class Invalid_Parameters_Request {

            @DisplayName("null 기존 사용자로 요청 시 예외가 발생한다.")
            @Test
            void updateUserInfo_withNullExistingUser_throwsException() {
                // given
                Email newEmail = Email.of("new@example.com");
                Gender newGender = Gender.FEMALE;
                BirthDate newBirthDate = BirthDate.of("1995-01-01");

                // when & then
                assertThatThrownBy(() -> userDomainService.updateUserInfo(null, newEmail, newGender, newBirthDate))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("null 새 이메일로 요청 시 예외가 발생한다.")
            @Test
            void updateUserInfo_withNullNewEmail_throwsException() {
                // given
                UserModel existingUser = UserModel.of(
                        UserId.of("testuser"),
                        Email.of("old@example.com"),
                        Gender.MALE,
                        BirthDate.of("1990-01-01")
                );
                Gender newGender = Gender.FEMALE;
                BirthDate newBirthDate = BirthDate.of("1995-01-01");

                // when & then
                assertThatThrownBy(() -> userDomainService.updateUserInfo(existingUser, null, newGender, newBirthDate))
                        .isInstanceOf(IllegalArgumentException.class);
            }

            @DisplayName("null 새 생년월일로 요청 시 예외가 발생한다.")
            @Test
            void updateUserInfo_withNullNewBirthDate_throwsException() {
                // given
                UserModel existingUser = UserModel.of(
                        UserId.of("testuser"),
                        Email.of("old@example.com"),
                        Gender.MALE,
                        BirthDate.of("1990-01-01")
                );
                Email newEmail = Email.of("new@example.com");
                Gender newGender = Gender.FEMALE;

                // when & then
                assertThatThrownBy(() -> userDomainService.updateUserInfo(existingUser, newEmail, newGender, null))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

}
