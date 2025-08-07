package com.loopers.domain.user;

import org.springframework.stereotype.Component;

@Component
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 회원 가입
     */
    public UserModel createUser(UserId userId, Email email, Gender gender, BirthDate birthDate) {
        validateUserCreation(userId, email, birthDate);
        return UserModel.of(userId, email, gender, birthDate);
    }

    /**
     * 사용자 정보 수정
     */
    public UserModel updateUserInfo(UserModel existingUser, Email newEmail, Gender newGender, BirthDate newBirthDate) {
        validateUserUpdate(existingUser, newEmail, newBirthDate);
        
        // 새로운 사용자 객체 생성 (불변성 유지)
        return UserModel.of(existingUser.getUserId(), newEmail, newGender, newBirthDate);
    }

    /**
     * 사용자 검증
     */
    private void validateUserCreation(UserId userId, Email email, BirthDate birthDate) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (email == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("생년월일은 필수입니다.");
        }
    }

    /**
     * 사용자 정보 수정 검증
     */
    private void validateUserUpdate(UserModel existingUser, Email newEmail, BirthDate newBirthDate) {
        if (existingUser == null) {
            throw new IllegalArgumentException("수정할 사용자가 존재하지 않습니다.");
        }
        if (newEmail == null) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (newBirthDate == null) {
            throw new IllegalArgumentException("생년월일은 필수입니다.");
        }
    }

}
