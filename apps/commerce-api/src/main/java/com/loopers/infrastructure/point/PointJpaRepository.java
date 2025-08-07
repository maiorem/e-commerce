package com.loopers.infrastructure.point;

import com.loopers.domain.user.UserId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.point.PointModel;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointModel, Long>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointModel p WHERE p.userId = :userId")
    Optional<PointModel> findByUserIdForUpdate(@Param("userId") UserId userId);

    Optional<PointModel> findByUserId(@Param("userId") UserId userId);

}
