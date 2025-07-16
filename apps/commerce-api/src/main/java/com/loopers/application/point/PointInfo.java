package com.loopers.application.point;

import com.loopers.domain.point.PointModel;

public record PointInfo(
    String userId,
    int totalAmount
) {
    public static PointInfo from(PointModel point) {
        return new PointInfo(point.getUserId().getValue(), point.getAmount());
    }
}
