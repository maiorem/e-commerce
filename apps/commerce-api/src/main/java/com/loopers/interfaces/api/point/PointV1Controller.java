package com.loopers.interfaces.api.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.web.bind.annotation.*;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.PointV1Dto.PointRequest;
import com.loopers.interfaces.api.point.PointV1Dto.PointResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec{
    
    private final PointFacade pointFacade;

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointResponse> chargePoint(
        @RequestHeader("X-USER-ID") String userId, 
        @RequestBody PointRequest request
    ) {
        PointInfo info = pointFacade.chargeMyPoint(userId, request.amount());

        PointV1Dto.PointResponse response = PointV1Dto.PointResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Override
    public ApiResponse<Object> getPoint(
            @RequestHeader(value = "X-USER-ID", required = false) String userId) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 필요합니다.");
        }
        PointInfo info = pointFacade.getMyPoint(userId);
        PointV1Dto.PointResponse response = PointV1Dto.PointResponse.from(info);
        return ApiResponse.success(response);
    }

}
