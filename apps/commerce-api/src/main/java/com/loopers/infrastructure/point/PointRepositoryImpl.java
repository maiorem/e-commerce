package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    @Override
    public Optional<PointModel> findByUserId(UserId userId) {
        return pointJpaRepository.findByUserId(userId);
    }

    @Override
    public PointModel create(PointModel point) {
        return pointJpaRepository.saveAndFlush(point);
    }
}
