package com.loopers;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 * Master-Readonly Replica 구조를 지원합니다.
 */
@Configuration
@EnableConfigurationProperties({RedisProperties.class})
public class RedisConfig {

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    /**
     * Master 전용 Redis ConnectionFactory
     */
    @Bean
    @Primary
    public RedisConnectionFactory masterConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getMaster().getHost());
        config.setPort(redisProperties.getMaster().getPort());
        config.setDatabase(redisProperties.getDatabase());
        
        return new LettuceConnectionFactory(config);
    }

    /**
     * Master 전용 RedisTemplate
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> masterRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(masterConnectionFactory());
        
        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Readonly Replica용 Redis ConnectionFactory (필요시 사용)
     */
    @Bean
    public RedisConnectionFactory readonlyConnectionFactory() {
        if (redisProperties.getReplicas() == null || redisProperties.getReplicas().isEmpty()) {
            return masterConnectionFactory();
        }
        
        // 첫 번째 replica 사용
        RedisNodeInfo replica = redisProperties.getReplicas().get(0);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(replica.getHost());
        config.setPort(replica.getPort());
        config.setDatabase(redisProperties.getDatabase());
        
        return new LettuceConnectionFactory(config);
    }

    /**
     * Readonly Replica용 RedisTemplate (필요시 사용)
     */
    @Bean
    public RedisTemplate<String, Object> readonlyRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(readonlyConnectionFactory());
        
        // 직렬화 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
