package com.loopers.application.point;

import org.springframework.stereotype.Component;

import com.loopers.domain.point.PointModel;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointApplicationService pointApplicationService;

    public PointInfo chargeMyPoint(String userId, int amount) {
        PointModel model = pointApplicationService.chargeMyPoint(userId, amount);
        return PointInfo.from(model);
    }

    public PointInfo getMyPoint(String userId) {
        PointModel model = pointApplicationService.getMyPoint(userId);
        return PointInfo.from(model);
    }
}
