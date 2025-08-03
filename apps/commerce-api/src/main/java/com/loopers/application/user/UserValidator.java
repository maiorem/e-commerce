package com.loopers.application.user;

import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserValidator {

    private final UserRepository userRepository;

    public void validateUserExists(UserId userId) {
        if(!userRepository.existsByUserId(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다. 사용자 ID: " + userId);
        }
    }
}
