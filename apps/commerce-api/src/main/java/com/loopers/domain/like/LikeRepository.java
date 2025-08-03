package com.loopers.domain.like;

import com.loopers.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    
    LikeModel save(LikeModel like);
    
    Optional<LikeModel> findByUserIdAndProductId(UserId userId, Long productId);

    int countByProductId(Long productId);
    
    List<LikeModel> findByUserId(UserId userId);

    void delete(LikeModel like);
    
    boolean existsByUserIdAndProductId(UserId userId, Long productId);

} 
