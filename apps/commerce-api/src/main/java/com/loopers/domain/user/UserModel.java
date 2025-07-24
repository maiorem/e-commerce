package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Table(name = "user")
public class UserModel extends BaseEntity {

    @Embedded
    private UserId userId;

    @Embedded
    private Email email;

    @Column(name = "gender", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Embedded
    private BirthDate birthDate;

    protected UserModel() {}

    @Builder
    public UserModel(UserId userId, Email email, Gender gender, BirthDate birthDate) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 비어있을 수 없습니다.");
        }
        if (email == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일은 비어있을 수 없습니다.");
        }
        if (gender == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 비어있을 수 없습니다.");
        }
        if (birthDate == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 비어있을 수 없습니다.");
        }
        this.userId = userId;
        this.email = email;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public UserId getUserId() {
        return userId;
    }

    public Email getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public BirthDate getBirthDate() {
        return birthDate;
    }
}
