package com.loopers.support.cache.config;

import com.loopers.infrastructure.redis.RedisCacheRepositoryImpl;
import com.loopers.support.cache.repository.CacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class CacheConfig {

    /**
     * Redis 캐시 저장소 (기본값)
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheRepository redisCacheRepository(RedisCacheRepositoryImpl redisCacheRepositoryImpl) {
        return redisCacheRepositoryImpl;
    }

}
