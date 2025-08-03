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
    public UserInfo createUser(String userId, String email, String gender, String birthDate)  {
        UserId regUserId = UserId.of(userId);
        Email regEmail = Email.of(email);
        Gender regGender = Gender.valueOf(gender);
        BirthDate regBirthDate = BirthDate.of(birthDate);
        // 중복 사용자 검증
        if (userRepository.existsByUserId(regUserId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 사용자 ID입니다.");
        }

        UserModel user = userDomainService.createUser(regUserId, regEmail, regGender, regBirthDate);
        userRepository.save(user);
        return UserInfo.from(user);
    }

    public UserInfo getUser(String userId) {
        UserId myUserId = UserId.of(userId);
        return userRepository.findByUserId(myUserId)
                .map(UserInfo::from)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다."));

    }

    @Transactional
    public UserModel updateUserInfo(UserId userId, Email newEmail, Gender newGender, BirthDate newBirthDate) {
        // 기존 사용자 조회
        UserModel existingUser = userRepository.findByUserId(userId).orElseThrow(
            () -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.")
        );

        UserModel updatedUser = userDomainService.updateUserInfo(existingUser, newEmail, newGender, newBirthDate);
        
        return userRepository.save(updatedUser);
    }

    @Transactional
    public void withdrawUser(UserId userId) {
        UserModel user = userRepository.findByUserId(userId).orElseThrow(
                () -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.")
        );
        userRepository.delete(user);
    }

}
