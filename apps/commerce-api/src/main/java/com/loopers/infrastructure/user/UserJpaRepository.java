package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserModel, Long> {
    boolean existsByUserId_Value(String value);
    Optional<UserModel> findByUserId_Value(String value);
}
