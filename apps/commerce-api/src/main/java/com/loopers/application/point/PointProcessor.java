package com.loopers.application.point;

import com.loopers.domain.order.OrderUsePointDomainService;
import com.loopers.domain.point.*;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointProcessor {

    private final PointRepository pointRepository;
    private final OrderUsePointDomainService orderUsePointDomainService;
    private final PointDomainService pointDomainService;

    @Transactional
    public int processPointUsage(UserId userId, int orderPrice, int requestPoint) {

        PointModel availablePoint = pointRepository.findByUserIdForUpdate(userId).orElse(null);

        // 주문에서 사용하기로 한 포인트 검증
        int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, requestPoint);

        // 포인트 사용
        if (usedPoints > 0 && availablePoint != null) {
            availablePoint.use(usedPoints);
            pointRepository.save(availablePoint);

            // 포인트 사용 내역 저장
            PointHistoryModel pointHistory = pointDomainService.createPointHistory(userId, usedPoints, availablePoint.getAmount(), PointChangeReason.ORDER);
            pointRepository.saveHistory(pointHistory);
        }
        return usedPoints;
    }

    public void restorePoint(UserId userId, int usedPoints) {
        PointModel availablePoint = pointRepository.findByUserIdForUpdate(userId).orElse(null);

        if (availablePoint != null) {
            // 포인트 복원
            availablePoint.restorePoint(usedPoints);
            pointRepository.save(availablePoint);

            // 포인트 복원 내역 저장
            PointHistoryModel pointHistory = pointDomainService.createPointHistory(userId, usedPoints, availablePoint.getAmount(), PointChangeReason.ORDER_RESTORE);
            pointRepository.saveHistory(pointHistory);
        }
    }
}
