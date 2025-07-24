package com.loopers.interfaces.api.user;

import org.springframework.web.bind.annotation.RequestHeader;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "회원 API")
public interface UserV1ApiSpec {

    @Operation(summary = "회원 가입", description = "회원정보를 받아 가입을 진행합니다.")
    ApiResponse<UserV1Dto.UserResponse> createUser(UserV1Dto.UserRequest request);

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회합니다.")
    ApiResponse<UserV1Dto.UserResponse> getMyInfo( @RequestHeader("X-USER-ID") String userId);

}
