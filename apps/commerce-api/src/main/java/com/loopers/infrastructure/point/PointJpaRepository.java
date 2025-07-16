package com.loopers.infrastructure.point;

import com.loopers.domain.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.point.PointModel;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointModel, Long>{

    Optional<PointModel> findByUserId(UserId userId);

}
