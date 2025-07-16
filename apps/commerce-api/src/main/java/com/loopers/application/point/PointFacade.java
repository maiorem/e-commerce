package com.loopers.application.point;

import org.springframework.stereotype.Component;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    public PointInfo chargeMyPoint(String userId, int amount) {
        PointModel model = pointService.chargeMyPoint(userId, amount);
        return PointInfo.from(model);
    }

    public PointInfo getMyPoint(String userId) {
        PointModel model = pointService.getMyPoint(userId);
        return PointInfo.from(model);
    }
}
