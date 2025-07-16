package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.RequestHeader;

import com.loopers.interfaces.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Point V1 API", description = "포인트 조회/충전 API")
public interface PointV1ApiSpec {

    @Operation(summary = "포인트 충전", description = "포인트를 충전합니다.")
    ApiResponse<PointV1Dto.PointResponse> chargePoint(@RequestHeader("X-USER-ID") String userId, @RequestBody PointV1Dto.PointRequest request);

    @Operation(summary = "포인트 조회", description = "사용자의 포인트 정보를 조회합니다.")
    ApiResponse<Object> getPoint(@RequestHeader("X-USER-ID") String userId);

}
