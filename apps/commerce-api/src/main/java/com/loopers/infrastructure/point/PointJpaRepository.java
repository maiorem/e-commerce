package com.loopers.infrastructure.point;

import com.loopers.domain.user.UserId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.point.PointModel;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointModel, Long>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointModel> findByUserId(UserId userId);

}
