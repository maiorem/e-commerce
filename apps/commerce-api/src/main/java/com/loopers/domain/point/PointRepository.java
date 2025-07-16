package com.loopers.domain.point;

import com.loopers.domain.user.UserId;

import java.util.Optional;

public interface PointRepository {

    Optional<PointModel> findByUserId(UserId userId);

    PointModel create(PointModel point);
}
