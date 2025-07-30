package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeJpaRepository likeJpaRepository;

    @Override
    public LikeModel save(LikeModel like) {
        return likeJpaRepository.save(like);
    }

    @Override
    public Optional<LikeModel> findByUserIdAndProductId(UserId userId, Long productId) {
        return likeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public int countByProductId(Long productId) {
        return likeJpaRepository.countByProductId(productId);
    }

    @Override
    public List<LikeModel> findByUserId(UserId userId) {
        return likeJpaRepository.findAllByUserId(userId);
    }

    @Override
    public List<LikeModel> findByProductId(Long productId) {
        return likeJpaRepository.findAllByProductId(productId);
    }



    @Override
    public void delete(LikeModel like) {

    }

    @Override
    public boolean existsByUserIdAndProductId(UserId userId, Long productId) {
        return likeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }
}
