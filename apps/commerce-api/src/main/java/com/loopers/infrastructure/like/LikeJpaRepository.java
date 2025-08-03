package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeModel, Long> {
    Optional<LikeModel> findByUserIdAndProductId(UserId userId, Long productId);

    List<LikeModel> findAllByUserId(UserId userId);

    List<LikeModel> findAllByProductId(Long productId);

    boolean existsByUserIdAndProductId(UserId userId, Long productId);

    int countByProductId(Long productId);
}
