package com.loopers.support.config;


import com.loopers.application.like.LikeCountEventHandler;
import com.loopers.application.product.UserActingTrackingForProductEventHandler;
import com.loopers.domain.user.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@TestConfiguration
public class TestConfig {

    @Bean
    public UserRepository userRepository(UserRepository realUserRepository) {
        return spy(realUserRepository);
    }

    @Bean
    @Primary
    public LikeCountEventHandler mockLikeEventHandler() {
        return mock(LikeCountEventHandler.class);
    }

    @Bean
    @Primary
    public UserActingTrackingForProductEventHandler mockProductEventHandler() {
        return mock(UserActingTrackingForProductEventHandler.class);
    }

}
