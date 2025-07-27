package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserDomainTest {

    private UserId createValidUserId() {
        return UserId.of("member1");
    }

    private Email createValidEmail() {
        return Email.of("test@example.com");
    }

    private BirthDate createValidBirthDate() {
        return BirthDate.of("2000-01-01");
    }

    @DisplayName("UserId 객체를 생성할 때,")
    @Nested
    class UserIdTest {

        @DisplayName("유효한 아이디가 주어지면 정상적으로 생성된다.")
        @Test
        void createUserId_withValidId() {
            // given
            String validId = "member1";

            // when
            UserId userId = UserId.of(validId);

            // then
            assertThat(userId.getValue()).isEqualTo(validId);
        }

        @DisplayName("아이디가 4자 미만이거나 10자를 초과하면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {"abc", "longlonguserid"})
        void createUserId_withInvalidLength_throwsException(String invalidId) {
            // when
            CoreException exception = assertThrows(CoreException.class, () -> UserId.of(invalidId));

            // then
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("아이디에 허용되지 않은 특수문자가 포함되면 예외가 발생한다.")
        @Test
        void createUserId_withSpecialCharacters_throwsException() {
            // given
            String invalidId = "user!@#";

            // when
            CoreException exception = assertThrows(CoreException.class, () -> UserId.of(invalidId));

            // then
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("Email 객체를 생성할 때,")
    @Nested
    class EmailTest {

        @DisplayName("유효한 이메일 주소가 주어지면 정상적으로 생성된다.")
        @Test
        void createEmail_withValidEmail() {
            // given
            String validEmail = "test@example.com";

            // when
            Email email = Email.of(validEmail);

            // then
            assertThat(email.getValue()).isEqualTo(validEmail);
        }

        @DisplayName("유효하지 않은 이메일 형식이 주어지면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {"seyoung", "seyoung@", "@loopers.com", "seyoung@loopers"})
        void createEmail_withInvalidEmail_throwsException(String invalidEmail) {
            // when
            CoreException exception = assertThrows(CoreException.class, () -> Email.of(invalidEmail));

            // then
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("BirthDate 객체를 생성할 때,")
    @Nested
    class BirthDateTest {

        @DisplayName("유효한 날짜 문자열(yyyy-MM-dd)이 주어지면 정상적으로 생성된다.")
        @Test
        void createBirthDate_withValidDateString() {
            // given
            String validDate = "2000-01-01";

            // when
            BirthDate birthDate = BirthDate.of(validDate);

            // then
            assertThat(birthDate.getValue().toString()).isEqualTo(validDate);
        }

        @DisplayName("유효하지 않은 날짜 형식이나 존재하지 않는 날짜가 주어지면 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {"2000/01/01", "2000-1-1", "2000-02-30"})
        void createBirthDate_withInvalidDateString_throwsException(String invalidDate) {
            // when
            CoreException exception = assertThrows(CoreException.class, () -> BirthDate.of(invalidDate));

            // then
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("UserModel 객체를 생성할 때,")
    @Nested
    class UserModelTest {

        @DisplayName("모든 정보가 유효하면 정상적으로 생성된다.")
        @Test
        void createUserModel_withValidData() {
            // given
            UserId userId = UserId.of("seyoung123");
            Email email = Email.of("hong@loopers.com");
            Gender gender = Gender.FEMALE;
            BirthDate birthDate = BirthDate.of("2000-01-01");

            // when
            UserModel user = UserModel.builder()
                                .userId(userId)
                                .email(email)
                                .gender(gender)
                                .birthDate(birthDate)
                                .build();

            // then
            assertAll(
                () -> assertThat(user.getUserId()).isEqualTo(userId),
                () -> assertThat(user.getEmail()).isEqualTo(email),
                () -> assertThat(user.getGender()).isEqualTo(gender),
                () -> assertThat(user.getBirthDate()).isEqualTo(birthDate)
            );
        }

        @DisplayName("필수 정보 중 하나라도 null이면 예외가 발생한다.")
        @Test
        void createUserModel_withNullData_throwsException() {
            // given
            UserId userId = UserId.of("seyoung123");
            Email email = Email.of("hong@loopers.com");
            Gender gender = Gender.FEMALE;
            BirthDate birthDate = BirthDate.of("2000-01-01");

            // then
            assertAll(
                () -> assertThrows(CoreException.class, () -> UserModel.builder().email(email).gender(gender).birthDate(birthDate).build()),
                () -> assertThrows(CoreException.class, () -> UserModel.builder().userId(userId).gender(gender).birthDate(birthDate).build()),
                () -> assertThrows(CoreException.class, () -> UserModel.builder().userId(userId).email(email).birthDate(birthDate).build()),
                () -> assertThrows(CoreException.class, () -> UserModel.builder().userId(userId).email(email).gender(gender).build())
            );
        }
    }

    @DisplayName("UserService 객체를 생성할 때,")
    @Nested
    @ExtendWith(MockitoExtension.class)
    class UserServiceTest {

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private UserService userService;

        @DisplayName("이미 존재하는 아이디로 회원가입을 시도하면 예외가 발생한다.")
        @Test
        void createUser_withExistingUserId_throwsException() {
            // given
            UserId existingUserId = createValidUserId();
            Email email = createValidEmail();
            Gender gender = Gender.MALE;
            BirthDate birthDate = createValidBirthDate();

            when(userRepository.existsByUserId(existingUserId)).thenReturn(true);

            // when & then
            CoreException exception = assertThrows(CoreException.class, () ->
                    userService.createUser(existingUserId, email, gender, birthDate)
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).isEqualTo("이미 존재하는 아이디입니다.");
        }

        @DisplayName("새로운 아이디로 회원가입을 시도하면 정상적으로 생성된다.")
        @Test
        void createUser_withNewUserId_createsUser() {
            // given
            UserId newUserId = createValidUserId();
            Email email = createValidEmail();
            Gender gender = Gender.MALE;
            BirthDate birthDate = createValidBirthDate();

            UserModel expectedUser = UserModel.builder()
                    .userId(newUserId)
                    .email(email)
                    .gender(gender)
                    .birthDate(birthDate)
                    .build();

            when(userRepository.existsByUserId(newUserId)).thenReturn(false);
            when(userRepository.create(any(UserModel.class))).thenReturn(expectedUser);

            // when
            UserModel createdUser = userService.createUser(newUserId, email, gender, birthDate);

            // then
            assertThat(createdUser).isEqualTo(expectedUser);
        }
    }
}
