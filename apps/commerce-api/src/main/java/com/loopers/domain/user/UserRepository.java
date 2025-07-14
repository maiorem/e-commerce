package com.loopers.domain.user;

public interface UserRepository {

    UserModel create(UserModel user);
    boolean existsByUserId(UserId userId);

}
