package com.loopers.application.user;

import com.loopers.domain.user.UserModel;


public record UserInfo(String userId, String email, String gender, String birthDate) {

    public static UserInfo from(UserModel model) {
        return new UserInfo(
                model.getUserId().getValue(),
                model.getEmail().getValue(),
                model.getGender().name(),
                model.getBirthDate().getValue().toString()
        );
    }

}
