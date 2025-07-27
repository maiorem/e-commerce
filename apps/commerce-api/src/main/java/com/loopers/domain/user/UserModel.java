package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "user")
@Getter
public class UserModel extends BaseEntity {

    @Embedded
    private UserId userId;

    @Embedded
    private Email email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Embedded
    private BirthDate birthDate;

    protected UserModel() {}

    public static UserModel of(UserId userId, Email email, Gender gender, BirthDate birthDate) {
        UserModel user = new UserModel();
        user.userId = userId;
        user.email = email;
        user.gender = gender;
        user.birthDate = birthDate;
        return user;
    }
}
