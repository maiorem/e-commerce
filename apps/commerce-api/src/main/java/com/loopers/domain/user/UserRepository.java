package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {

    UserModel create(UserModel user);
    boolean existsByUserId(UserId userId);
    Optional<UserModel> findByUserId(UserId userId);

}
