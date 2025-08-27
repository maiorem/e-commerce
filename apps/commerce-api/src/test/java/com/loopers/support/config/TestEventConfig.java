package com.loopers.support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@TestConfiguration
@EnableAsync
public class TestEventConfig implements AsyncConfigurer {

    @Bean
    @Primary
    @Override
    public Executor getAsyncExecutor() {
        return new Executor() {
            @Override
            public void execute(Runnable command) {
                command.run();
            }
        };
    }
}
