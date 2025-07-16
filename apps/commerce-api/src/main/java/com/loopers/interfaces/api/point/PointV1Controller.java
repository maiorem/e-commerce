package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.PointV1Dto.PointRequest;
import com.loopers.interfaces.api.point.PointV1Dto.PointResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vi/points")
public class PointV1Controller implements PointV1ApiSpec{
    
    private final PointFacade pointFacade;

    @PostMapping()
    @Override
    public ApiResponse<PointResponse> chargePoint(
        @RequestHeader("X-USER-ID") String userId, 
        @RequestBody PointRequest request
    ) {
        PointInfo info = pointFacade.chargeMyPoint(userId, request.amount());

        PointV1Dto.PointResponse response = PointV1Dto.PointResponse.from(info);
        return ApiResponse.success(response);
    }

}
