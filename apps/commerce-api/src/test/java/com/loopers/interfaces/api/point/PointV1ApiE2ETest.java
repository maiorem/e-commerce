package com.loopers.interfaces.api.point;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.loopers.interfaces.api.ApiResponse;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

    private static final String ENDPOINT_CHARGE_POINT = "/api/v1/points/charge";
    private static final String ENDPOINT_GET_POINT = "/api/v1/points";

    private final TestRestTemplate testRestTemplate;
    private final


    @DisplayName("POST /api/vi/points/charge")
    @Nested
    class ChargePoint {

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void chargePoint_withExistingUser_returnsChargedTotalAmount() {
            // given
            String userId = "seyoung";
            int amount = 1000;

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", userId);

            PointV1Dto.PointRequest request = new PointV1Dto.PointRequest(amount);

            // when
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CHARGE_POINT, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // then
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody().data().totalAmount()).isEqualTo(amount)
            );
        }

}
