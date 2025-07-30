package com.loopers.application.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.user.UserId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeLikeRepository implements LikeRepository {
    
    private final Map<String, LikeModel> likes = new ConcurrentHashMap<>();
    
    @Override
    public LikeModel save(LikeModel like) {
        String key = generateKey(like.getUserId(), like.getProductId());
        likes.put(key, like);
        return like;
    }
    
    @Override
    public void delete(LikeModel like) {
        String key = generateKey(like.getUserId(), like.getProductId());
        likes.remove(key);
    }
    
    @Override
    public boolean existsByUserIdAndProductId(UserId userId, Long productId) {
        String key = generateKey(userId, productId);
        return likes.containsKey(key);
    }
    
    @Override
    public Optional<LikeModel> findByUserIdAndProductId(UserId userId, Long productId) {
        String key = generateKey(userId, productId);
        return Optional.ofNullable(likes.get(key));
    }
    
    @Override
    public List<LikeModel> findByUserId(UserId userId) {
        return likes.values().stream()
                .filter(like -> like.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public int countByProductId(Long productId) {
        return (int) likes.values().stream()
                .filter(like -> like.getProductId().equals(productId))
                .count();
    }
    
    private String generateKey(UserId userId, Long productId) {
        return userId.getValue() + ":" + productId;
    }
    
    // 테스트용 메서드들
    public void clear() {
        likes.clear();
    }
    
    public int size() {
        return likes.size();
    }
    
    public boolean isEmpty() {
        return likes.isEmpty();
    }
} 
