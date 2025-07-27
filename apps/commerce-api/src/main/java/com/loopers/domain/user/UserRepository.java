package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {
    
    UserModel save(UserModel user);
    
    Optional<UserModel> findByUserId(UserId userId);
    
    boolean existsByUserId(UserId userId);
    
    void delete(UserModel user);
}
