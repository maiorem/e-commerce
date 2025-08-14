package com.loopers.support.cache.util;

import com.loopers.support.cache.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 캐시 유틸리티 클래스
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheUtil {

    private final CacheRepository cacheRepository;

    /**
     * 객체를 캐시에 저장
     */
    public <T> void set(String key, T value, Duration ttl) {
        cacheRepository.set(key, value, ttl);
    }

    /**
     * 기본 TTL로 객체 저장 (10분)
     */
    public <T> void set(String key, T value) {
        cacheRepository.set(key, value);
    }

    /**
     * 캐시에서 객체 조회
     */
    public <T> Optional<T> get(String key, Class<T> clazz) {
        return cacheRepository.get(key, clazz);
    }

    /**
     * 캐시에서 List 객체 조회
     */
    public <T> Optional<List<T>> getList(String key, Class<T> clazz) {
        return cacheRepository.getList(key, clazz);
    }

    /**
     * 캐시 존재 여부 확인
     */
    public boolean exists(String key) {
        return cacheRepository.exists(key);
    }

    /**
     * 캐시 삭제
     */
    public void delete(String key) {
        cacheRepository.delete(key);
    }

    /**
     * 패턴에 맞는 모든 키 삭제
     */
    public void deleteByPattern(String pattern) {
        cacheRepository.deleteByPattern(pattern);
    }

    /**
     * TTL 조회
     */
    public Duration getTtl(String key) {
        return cacheRepository.getTtl(key);
    }

    /**
     * TTL 설정
     */
    public void expire(String key, Duration ttl) {
        cacheRepository.expire(key, ttl);
    }

} 
