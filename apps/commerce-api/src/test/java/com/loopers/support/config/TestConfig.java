package com.loopers.support.config;


import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.spy;

@TestConfiguration
public class TestConfig {

    // Mockito.spy()를 사용해 스파이 객체를 직접 생성
    @Bean
    public UserService userService(UserService realUserService) {
        return spy(realUserService);
    }

    @Bean
    public UserRepository userRepository(UserRepository realUserRepository) {
        return spy(realUserRepository);
    }


}
