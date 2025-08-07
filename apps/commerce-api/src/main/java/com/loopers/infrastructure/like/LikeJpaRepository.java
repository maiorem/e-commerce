package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.user.UserId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeModel, Long> {
    Optional<LikeModel> findByUserIdAndProductId(UserId userId, Long productId);

    List<LikeModel> findAllByUserId(UserId userId);

    List<LikeModel> findAllByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsByUserIdAndProductId(UserId userId, Long productId);

    int countByProductId(Long productId);
}
