package com.loopers.domain.point;

import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class PointDomainService {

    /**
     * 포인트 충전
     */
    public PointModel chargePoint(UserId userId, int amount) {
        validateChargeAmount(amount);
        
        // 포인트 생성 (기존 포인트가 없을 경우)
        PointModel point = PointModel.of(userId, 0);
        point.charge(amount);
        
        return point;
    }

    /**
     * 기존 포인트에 충전
     */
    public PointModel chargePoint(PointModel existingPoint, int amount) {
        validateChargeAmount(amount);
        
        PointModel updatedPoint = PointModel.of(existingPoint.getUserId(), existingPoint.getAmount());
        updatedPoint.charge(amount);
        
        return updatedPoint;
    }

    /**
     * 포인트 사용
     */
    public PointModel usePoint(PointModel existingPoint, int amount) {
        validateUseAmount(amount);
        validateSufficientBalance(existingPoint, amount);
        
        PointModel updatedPoint = PointModel.of(existingPoint.getUserId(), existingPoint.getAmount());
        updatedPoint.use(amount);
        
        return updatedPoint;
    }

    /**
     * 포인트 환불
     */
    public PointModel refundPoint(PointModel existingPoint, int amount) {
        validateRefundAmount(amount);
        
        PointModel updatedPoint = PointModel.of(existingPoint.getUserId(), existingPoint.getAmount());
        updatedPoint.charge(amount);
        
        return updatedPoint;
    }

    /**
     * 포인트 내역 생성 도메인 로직
     */
    public PointHistoryModel createPointHistory(UserId userId, int changedAmount, int currentAmount, PointChangeReason reason) {
        validatePointHistory(userId, changedAmount, currentAmount, reason);
        return PointHistoryModel.of(userId, changedAmount, currentAmount, reason);
    }

    /**
     * 충전 금액 검증
     */
    private void validateChargeAmount(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }
        if (amount > 1000000) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 1,000,000원을 초과할 수 없습니다.");
        }
    }

    /**
     * 사용 금액 검증
     */
    private void validateUseAmount(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용할 포인트는 0보다 커야 합니다.");
        }
    }

    /**
     * 잔액 부족 검증
     */
    private void validateSufficientBalance(PointModel point, int amount) {
        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "포인트 정보가 없습니다.");
        }
        if (point.getAmount() < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "잔여 포인트가 부족합니다.");
        }
    }

    /**
     * 환불 금액 검증
     */
    private void validateRefundAmount(int amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "환불 금액은 0보다 커야 합니다.");
        }
    }

    /**
     * 포인트 내역 검증
     */
    private void validatePointHistory(UserId userId, int changedAmount, int currentAmount, PointChangeReason reason) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 필수입니다.");
        }
        if (reason == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 변동 사유는 필수입니다.");
        }
        if (currentAmount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 잔액은 음수가 될 수 없습니다.");
        }
    }

    /**
     * 포인트 잔액 확인
     */
    public boolean hasSufficientPoint(PointModel point, int requiredAmount) {
        return point != null && point.getAmount() >= requiredAmount;
    }

    /**
     * 포인트 만료 여부 확인
     */
    public boolean isPointExpired(PointModel point) {
        if (point == null) {
            return false;
        }
        return point.isExpired();
    }

    /**
     * 만료된 포인트 처리
     */
    public PointModel expirePoint(PointModel point) {
        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "포인트 정보가 없습니다.");
        }
        
        if (point.isExpired()) {
            // 만료된 포인트는 0으로 설정
            return PointModel.of(point.getUserId(), 0, point.getExpiredAt());
        }
        return point;
    }
}
