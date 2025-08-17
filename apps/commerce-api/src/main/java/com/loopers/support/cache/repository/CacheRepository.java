package com.loopers.support.cache.repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface CacheRepository {

    /**
     * 객체를 캐시에 저장
     */
    <T> void set(String key, T value, Duration ttl);

    /**
     * 기본 TTL로 객체 저장
     */
    <T> void set(String key, T value);

    /**
     * 캐시에서 객체 조회
     */
    <T> Optional<T> get(String key, Class<T> clazz);

    /**
     * 캐시에서 List 객체 조회
     */
    <T> Optional<List<T>> getList(String key, Class<T> clazz);

    /**
     * 캐시 존재 여부 확인
     */
    boolean exists(String key);

    /**
     * 캐시 삭제
     */
    void delete(String key);

    /**
     * 패턴에 맞는 모든 키 삭제
     */
    void deleteByPattern(String pattern);

    /**
     * TTL 조회
     */
    Duration getTtl(String key);

    /**
     * TTL 설정
     */
    void expire(String key, Duration ttl);

} 
