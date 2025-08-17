package com.loopers.utils;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 테스트 데이터 정리 유틸리티
 */
@Component
public class RedisCleanUp {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCleanUp(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * 모든 Redis 데이터를 삭제합니다.
     */
    public void truncateAll() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
    }

    /**
     * 특정 데이터베이스의 모든 데이터를 삭제합니다.
     */
    public void truncateDatabase(int database) {
        redisConnectionFactory.getConnection().serverCommands().flushDb();
    }
} 