package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserModel save(UserModel user) {
        return userJpaRepository.saveAndFlush(user);
    }

    @Override
    public boolean existsByUserId(UserId userId) {
        return userJpaRepository.existsByUserId(userId);
    }

    @Override
    public Optional<UserModel> findByUserId(UserId userId) {
        return userJpaRepository.findByUserId(userId);
    }

    @Override
    public void delete(UserModel user) {
        userJpaRepository.delete(user);
    }
}
