package com.loopers.application.user;

import com.loopers.domain.user.BirthDate;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserApplicationService userApplicationService;

    public UserInfo createUser(String userId, String email, String gender, String birthDate) {
        UserId regUserId = UserId.of(userId);
        Email regEmail = Email.of(email);
        Gender regGender = Gender.valueOf(gender);
        BirthDate regBirthDate = BirthDate.of(birthDate);

        UserModel user = userApplicationService.createUser(regUserId, regEmail, regGender, regBirthDate);
        return UserInfo.from(user);
    }

    public UserInfo getMyInfo(String userId) {
        UserId myUserId = UserId.of(userId);
        UserModel user = userApplicationService.getUser(myUserId);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        return UserInfo.from(user);
    }
}
