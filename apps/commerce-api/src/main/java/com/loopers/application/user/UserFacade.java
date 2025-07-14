package com.loopers.application.user;

import com.loopers.domain.user.BirthDate;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;

    public UserInfo createUser(String userId, String email, String gender, String birthDate) {
        UserId regUserId = new UserId(userId);
        Email regEmail = new Email(email);
        Gender regGender = Gender.valueOf(gender);
        BirthDate regBirthDate = new BirthDate(birthDate);

        UserModel user = userService.createUser(regUserId, regEmail, regGender, regBirthDate);
        return UserInfo.from(user);
    }
}
