package com.loopers.support.config;


import com.loopers.domain.user.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.spy;

@TestConfiguration
public class TestConfig {

    @Bean
    public UserRepository userRepository(UserRepository realUserRepository) {
        return spy(realUserRepository);
    }


}
