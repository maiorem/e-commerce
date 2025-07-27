package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryModel;
import com.loopers.domain.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryModel, Long> {

    List<PointHistoryModel> findAllByUserId(UserId userId);
}
