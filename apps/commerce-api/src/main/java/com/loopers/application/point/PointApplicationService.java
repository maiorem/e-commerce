package com.loopers.application.point;

import com.loopers.domain.point.*;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointApplicationService {

    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointDomainService pointDomainService;

    @Transactional
    public PointInfo chargeMyPoint(String userId, int amount) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsByUserId(UserId.of(userId))) {
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }

        // 기존 포인트 조회 또는 새로 생성
        PointModel existingPoint = pointRepository.findByUserIdForUpdate(UserId.of(userId))
                .orElseGet(() -> PointModel.of(UserId.of(userId), 0));

        // 포인트 충전
        PointModel updatedPoint = pointDomainService.chargePoint(existingPoint, amount);
        
        // 저장
        PointModel savedPoint = pointRepository.save(updatedPoint);
        
        // 포인트 내역 생성 및 저장
        PointHistoryModel history = pointDomainService.createPointHistory(
                UserId.of(userId), 
                amount, 
                savedPoint.getAmount(), 
                PointChangeReason.PROMOTION
        );
        pointRepository.saveHistory(history);
        
        return PointInfo.from(savedPoint);
    }

    @Transactional
    public PointInfo useMyPoint(String userId, int amount) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsByUserId(UserId.of(userId))) {
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }

        // 기존 포인트 조회
        PointModel existingPoint = pointRepository.findByUserIdForUpdate(UserId.of(userId))
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보가 없습니다."));

        // 포인트 사용
        PointModel updatedPoint = pointDomainService.usePoint(existingPoint, amount);
        
        // 저장
        PointModel savedPoint = pointRepository.save(updatedPoint);
        
        // 포인트 내역 생성 및 저장
        PointHistoryModel history = pointDomainService.createPointHistory(
                UserId.of(userId), 
                -amount, 
                savedPoint.getAmount(), 
                PointChangeReason.ORDER
        );
        pointRepository.saveHistory(history);
        
        return PointInfo.from(savedPoint);
    }

    @Transactional
    public PointModel refundMyPoint(String userId, int amount) {

        // 사용자 존재 여부 확인
        if (!userRepository.existsByUserId(UserId.of(userId))) {
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }

        // 기존 포인트 조회
        PointModel existingPoint = pointRepository.findByUserIdForUpdate(UserId.of(userId))
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트 정보가 없습니다."));

        // 포인트 환불
        PointModel updatedPoint = pointDomainService.refundPoint(existingPoint, amount);

        // 저장
        PointModel savedPoint = pointRepository.save(updatedPoint);

        // 포인트 내역 생성 및 저장
        PointHistoryModel history = pointDomainService.createPointHistory(
                UserId.of(userId), 
                amount, 
                savedPoint.getAmount(), 
                PointChangeReason.REFUND
        );
        pointRepository.saveHistory(history);
        
        return savedPoint;
    }

    @Transactional(readOnly = true)
    public PointInfo getMyPoint(String userId) {

        // 사용자 존재 여부 확인
        if (!userRepository.existsByUserId(UserId.of(userId))) {
            return null;
        }
        // 포인트 조회 (일반 조회 메서드 사용)
        PointModel pointModel = pointRepository.findByUserIdForRead(UserId.of(userId)).orElse(null);
        if (pointModel == null) {
            return PointInfo.from(PointModel.of(UserId.of(userId), 0));
        }
        return PointInfo.from(pointModel);
    }

} 
