package com.loopers.interfaces.api.point;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.BirthDate;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserModel;
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
import org.springframework.http.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest {

    private static final String ENDPOINT_CHARGE_POINT = "/api/v1/points/charge";
    private static final String ENDPOINT_GET_POINT = "/api/v1/points";

    private final TestRestTemplate testRestTemplate;
    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointV1ApiE2ETest(
            TestRestTemplate testRestTemplate,
            PointRepository pointRepository,
            UserRepository userRepository,
            DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.pointRepository = pointRepository;
        this.userRepository = userRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class ChargePoint {

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void chargePoint_withExistingUser_returnsChargedTotalAmount() {
            // given
            UserModel user = userRepository.create(UserModel.builder()
                    .userId(new UserId("seyoung"))
                    .email(new Email("seyoung@loopers.com"))
                    .gender(Gender.MALE)
                    .birthDate(new BirthDate("2000-01-01"))
                    .build());

            // when
            int amount = 1000;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().getValue());

            PointV1Dto.PointRequest request = new PointV1Dto.PointRequest(amount);

            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {
            };
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_CHARGE_POINT, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(amount)
            );
        }

        @DisplayName("기존 유저가 1000원 충전 후 500원 추가 충전 시, 총 1500원을 응답으로 반환한다.")
        @Test
        void chargePoint_withExistingUser_addsToTotalAmount() {
            // given
            UserModel user = userRepository.create(UserModel.builder()
                    .userId(new UserId("testuser2"))
                    .email(new Email("testuser2@loopers.com"))
                    .gender(Gender.FEMALE)
                    .birthDate(new BirthDate("1995-05-05"))
                    .build());

            // 첫 번째 충전
            int initialAmount = 1000;
            HttpHeaders initialHeaders = new HttpHeaders();
            initialHeaders.set("X-USER-ID", user.getUserId().getValue());
            PointV1Dto.PointRequest initialRequest = new PointV1Dto.PointRequest(initialAmount);
            testRestTemplate.exchange(ENDPOINT_CHARGE_POINT, HttpMethod.POST, new HttpEntity<>(initialRequest, initialHeaders), new ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>>() {});

            // 두 번째 충전
            int additionalAmount = 500;
            HttpHeaders additionalHeaders = new HttpHeaders();
            additionalHeaders.set("X-USER-ID", user.getUserId().getValue());
            PointV1Dto.PointRequest additionalRequest = new PointV1Dto.PointRequest(additionalAmount);

            // when
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_CHARGE_POINT, HttpMethod.POST, new HttpEntity<>(additionalRequest, additionalHeaders), responseType);

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(initialAmount + additionalAmount)
            );
        }

        @DisplayName("존재하지 않는 유저가 충전 요청을 하면, 400 BAD_REQUEST 응답을 반환한다.")
        @Test
        void chargePoint_withNonExistingUser_returns400BadRequest() {
            // given
            String nonExistingUserId = "nonExistingUser";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", nonExistingUserId);
            PointV1Dto.PointRequest request = new PointV1Dto.PointRequest(1000);

            // when
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_CHARGE_POINT, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // then
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertTrue(response.getStatusCode().is4xxClientError())
            );
        }
    }
}
