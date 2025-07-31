package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserApplicationService;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserApplicationService userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> createUser(
            @RequestBody @Valid UserV1Dto.UserRequest request
    ) {
        UserInfo info = userFacade.createUser(
                request.userId(),
                request.email(),
                request.gender(),
                request.birthDate()
        );
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getMyInfo(
        @RequestHeader("X-USER-ID") String userId
    ) {
        UserInfo info = userFacade.getUser(userId);
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(info);
        return ApiResponse.success(response);
    }

}

