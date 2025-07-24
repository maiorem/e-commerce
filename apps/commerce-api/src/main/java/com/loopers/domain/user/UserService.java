package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserModel createUser(UserId userId, Email email, Gender gender, BirthDate birthDate) {
        if (userRepository.existsByUserId(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 아이디입니다.");
        }
        UserModel user = UserModel.builder()
                        .userId(userId)
                        .email(email)
                        .gender(gender)
                        .birthDate(birthDate)
                        .build();
        return userRepository.create(user);
    }

    public UserModel getUser(UserId userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }
}
