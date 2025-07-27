package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointHistoryModel;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public PointModel save(PointModel point) {
        return pointJpaRepository.saveAndFlush(point);
    }

    @Override
    public Optional<PointModel> findByUserId(UserId userId) {
        return pointJpaRepository.findByUserId(userId);
    }

    @Override
    public List<PointHistoryModel> findHistoryByUserId(UserId userId) {
        return pointHistoryJpaRepository.findAllByUserId(userId);
    }

    @Override
    public PointHistoryModel saveHistory(PointHistoryModel history) {
        return pointHistoryJpaRepository.saveAndFlush(history);
    }

    @Override
    public void delete(PointModel point) {
        pointJpaRepository.delete(point);
    }
}
