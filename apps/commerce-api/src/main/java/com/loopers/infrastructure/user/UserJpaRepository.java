package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserId;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserModel, Long> {
    boolean existsByUserId(UserId userId);
    Optional<UserModel> findByUserId(UserId userId);
}
