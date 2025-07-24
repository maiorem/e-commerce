package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserV1Dto {

    public record UserRequest(
            @NotBlank(message = "회원 아이디는 필수입니다.")
            @Size(min = 4, max = 10, message = "아이디는 4자 이상 10자 이하여야 합니다.")
            @Pattern(regexp = "^[a-zA-Z0-9]{4,10}$", message = "아이디는 영문 및 숫자만 가능합니다.")
            @Parameter(name = "회원 아이디", description = "영문 및 숫자 10자 이내")
            String userId,

            @NotBlank(message = "이메일은 필수입니다.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            @Parameter(name = "이메일", description = "xxx@yy.zz 형식으로 작성")
            String email,

            @NotBlank(message = "성별은 필수입니다.")
            @Pattern(regexp = "^(MALE|FEMALE)$", message = "성별은 MALE 또는 FEMALE이어야 합니다.")
            @Parameter(name = "성별", description = "MALE, FEMALE 선택")
            String gender,

            @NotBlank(message = "생년월일은 필수입니다.")
            @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년년월일 형식이 올바르지 않습니다. (yyyy-MM-dd)")
            @Parameter(name = "생년월일", description = "2000-01-01 형식으로 기입")
            String birthDate
    ) {
        public static UserRequest from(UserInfo userInfo) {
            return new UserRequest(
                userInfo.userId(),
                userInfo.email(),
                userInfo.gender(),
                userInfo.birthDate()
            );
        }
    }

    public record UserResponse(String userId, String email, String gender, String birthDate) {
        public static UserResponse from(UserInfo userInfo) {
            return new UserResponse(
                    userInfo.userId(),
                    userInfo.email(),
                    userInfo.gender(),
                    userInfo.birthDate()
            );
        }
    }

}
