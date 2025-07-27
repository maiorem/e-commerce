package com.loopers.application.user;

import com.loopers.domain.user.*;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    @Transactional
    public UserModel createUser(UserId userId, Email email, Gender gender, BirthDate birthDate) {
        // 중복 사용자 검증
        if (userRepository.existsByUserId(userId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 사용자 ID입니다.");
        }

        UserModel user = userDomainService.createUser(userId, email, gender, birthDate);
        
        return userRepository.save(user);
    }

    public UserModel getUser(UserId userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public UserModel updateUserInfo(UserId userId, Email newEmail, Gender newGender, BirthDate newBirthDate) {
        // 기존 사용자 조회
        UserModel existingUser = getUser(userId);
        if (existingUser == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }

        UserModel updatedUser = userDomainService.updateUserInfo(existingUser, newEmail, newGender, newBirthDate);
        
        return userRepository.save(updatedUser);
    }

    @Transactional
    public void withdrawUser(UserId userId) {
        UserModel user = getUser(userId);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
        }
        userRepository.delete(user);
    }

}
