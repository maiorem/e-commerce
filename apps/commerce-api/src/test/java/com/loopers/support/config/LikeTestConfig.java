package com.loopers.support.config;

import com.loopers.application.like.LikeCountProcessor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.spy;

@TestConfiguration
public class LikeTestConfig {

    @Bean
    @Primary
    public LikeCountProcessor likeCountProcessorSpy(LikeCountProcessor realLikeCountProcessor) {
        return spy(realLikeCountProcessor);
    }
}


