package com.loopers.infrastructure.redis;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCachePolicyConfig {

    @Primary
    @Bean
    public CacheManager cacheManager(
            LettuceConnectionFactory lettuceConnectionFactory,
            GenericJackson2JsonRedisSerializer redisValueSerializer
    ) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // 기본 TTL
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisValueSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(lettuceConnectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("productList",
                        config.entryTtl(Duration.ofMinutes(5))) // 상품목록은 5분
                .withCacheConfiguration("productDetail",
                        config.entryTtl(Duration.ofMinutes(30))) // 상품상세는 30분
                .withCacheConfiguration("brand",
                        config.entryTtl(Duration.ofHours(2))) // 브랜드는 2시간
                .withCacheConfiguration("category",
                        config.entryTtl(Duration.ofHours(2))) // 카테고리는 2시간
                .build();
    }
}
