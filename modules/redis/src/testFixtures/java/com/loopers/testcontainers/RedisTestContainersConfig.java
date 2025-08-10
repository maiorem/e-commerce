package com.loopers.testcontainers;

import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Redis 테스트 컨테이너 설정
 */
@Configuration
public class RedisTestContainersConfig {

    private static final GenericContainer<?> redisContainer;

    static {
        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    public static void setSystemProperties() {
        System.setProperty("datasource.redis.database", "0");
        System.setProperty("datasource.redis.master.host", redisContainer.getHost());
        System.setProperty("datasource.redis.master.port", String.valueOf(redisContainer.getMappedPort(6379)));
        
        // Replica도 같은 컨테이너 사용 (테스트용)
        System.setProperty("datasource.redis.replicas[0].host", redisContainer.getHost());
        System.setProperty("datasource.redis.replicas[0].port", String.valueOf(redisContainer.getMappedPort(6379)));
    }

    public static void stopContainer() {
        if (redisContainer != null && redisContainer.isRunning()) {
            redisContainer.stop();
        }
    }
} 