package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "회원 API")
public interface UserV1ApiSpec {

    @Operation(summary = "회원 가입", description = "회원정보를 받아 가입을 진행합니다.")
    ApiResponse<UserV1Dto.UserResponse> createUser(UserV1Dto.UserRequest request);

}
