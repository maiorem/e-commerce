package com.loopers.interfaces.api.user;

import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {

    private static final String ENDPOINT_CREATE_USER = "/api/v1/users";

    private final TestRestTemplate testRestTemplate;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        UserRepository userRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userRepository = userRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class CreateUser {

        @DisplayName("유효한 회원 정보가 주어지면, 회원가입에 성공하고 200 OK 응답을 반환한다.")
        @Test
        void createUser_withValidInfo_returns200Ok() {
            // given
            UserV1Dto.UserRequest request = new UserV1Dto.UserRequest(
                "seyoung", "seyoung@loopers.com", Gender.FEMALE.name(), "2000-01-01"
            );

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CREATE_USER, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(request.userId()),
                () -> assertThat(response.getBody().data().email()).isEqualTo(request.email())
            );
            assertThat(userRepository.existsByUserId(new UserId(request.userId()))).isTrue();
        }

        @DisplayName("이미 존재하는 아이디로 회원가입을 시도하면, 400 BAD_REQUEST 응답을 반환한다.")
        @Test
        void createUser_withExistingUserId_returns400BadRequest() {
            // given
            UserV1Dto.UserRequest existingUserRequest = new UserV1Dto.UserRequest(
                "existinguser", "seyoung@loopers.com", Gender.FEMALE.name(), "1990-05-10"
            );
            // 첫 번째 회원가입 (성공)
            testRestTemplate.exchange(ENDPOINT_CREATE_USER, HttpMethod.POST, new HttpEntity<>(existingUserRequest), new ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>>() {});

            // 중복 아이디로 다시 시도
            UserV1Dto.UserRequest duplicateUserRequest = new UserV1Dto.UserRequest(
                "existinguser", "seyoung_2@loopers.com", Gender.MALE.name(), "2001-01-01"
            );

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CREATE_USER, HttpMethod.POST, new HttpEntity<>(duplicateUserRequest), responseType);

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertTrue(response.getStatusCode().is4xxClientError())
            );
        }

        @DisplayName("유효하지 않은 이메일 형식으로 회원가입을 시도하면, 400 BAD_REQUEST 응답을 반환한다.")
        @Test
        void createUser_withInvalidEmail_returns400BadRequest() {
            // given
            UserV1Dto.UserRequest request = new UserV1Dto.UserRequest(
                "invalidemailuser", "invalid-email", Gender.MALE.name(), "2000-01-01"
            );

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CREATE_USER, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertTrue(response.getStatusCode().is4xxClientError())
            );
        }

        @DisplayName("필수 필드가 누락된 채로 회원가입을 시도하면, 400 BAD_REQUEST 응답을 반환한다.")
        @Test
        void createUser_withMissingField_returns400BadRequest() {
            // given
            UserV1Dto.UserRequest request = new UserV1Dto.UserRequest(
                "seyoung", "seyoung@loopers.com", "", "2000-01-01" // 성별 누락
            );

            // when
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CREATE_USER, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // then
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                () -> assertTrue(response.getStatusCode().is4xxClientError())
            );
        }
    }
}
