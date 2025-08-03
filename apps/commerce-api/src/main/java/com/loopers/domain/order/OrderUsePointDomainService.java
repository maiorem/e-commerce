package com.loopers.domain.order;

import com.loopers.domain.point.PointModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUsePointDomainService {

    /**
     * 주문에서 사용 할 포인트
     */
    public int calculateUsePoint(PointModel availablePoint, int orderPrice, int requestUsePoint) {
        if (availablePoint == null || availablePoint.getAmount() <= 0) {
            return 0; // 사용 가능한 포인트가 없으면 0 반환
        }
        if (requestUsePoint <= 0) {
            return 0; // 요청한 사용 포인트가 0 이하이면 0 반환
        }
        int maxUsablePoint = Math.min(availablePoint.getAmount(), orderPrice);
        return Math.min(maxUsablePoint, requestUsePoint);
    }

}
