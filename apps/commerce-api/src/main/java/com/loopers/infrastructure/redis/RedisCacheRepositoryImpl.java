package com.loopers.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.support.cache.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisCacheRepositoryImpl implements CacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public <T> void set(String key, T value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Redis 캐시 저장 완료 - 키: {}, TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Redis 객체 저장 실패 - 키: {}, 에러: {}", key, e.getMessage());
        }
    }

    @Override
    public <T> void set(String key, T value) {
        set(key, value, Duration.ofMinutes(10));
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            
            // Explicitly convert LinkedHashMap to target class using objectMapper
            T convertedValue = objectMapper.convertValue(value, clazz);

            if (convertedValue != null) {
                log.debug("Redis 캐시 조회 완료 - 키: {}", key);
                return Optional.of(convertedValue);
            } else {
                log.warn("Redis 캐시 변환 실패 - 키: {}, 예상 타입: {}, 실제 값: {}", 
                    key, clazz.getSimpleName(), value);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Redis 객체 조회 실패 - 키: {}, 에러: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<List<T>> getList(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            
            if (value instanceof List<?> list) {
                List<T> typedList = list.stream()
                                        .map(element -> objectMapper.convertValue(element, clazz))
                                        .collect(java.util.stream.Collectors.toList());
                log.debug("Redis List 캐시 조회 완료 - 키: {}", key);
                return Optional.of(typedList);
            } else {
                log.warn("Redis 캐시가 List 타입이 아님 - 키: {}, 실제 타입: {}", key, value.getClass().getSimpleName());
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Redis List 객체 조회 실패 - 키: {}, 에러: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Redis 캐시 삭제 완료 - 키: {}", key);
    }

    @Override
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Redis 패턴 캐시 삭제 완료 - 패턴: {}, 삭제된 키 수: {}", pattern, keys.size());
        }
    }

    @Override
    public Duration getTtl(String key) {
        Long ttlSeconds = redisTemplate.getExpire(key);
        return ttlSeconds != null ? Duration.ofSeconds(ttlSeconds) : Duration.ZERO;
    }

    @Override
    public void expire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
        log.debug("Redis TTL 설정 완료 - 키: {}, TTL: {}", key, ttl);
    }

} 
