package com.loopers.domain.point;

import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PointService {

    private final UserRepository userRepository;

    private final PointRepository pointRepository;


    @Transactional
    public PointModel chargeMyPoint(String userId, int amount) {
        if (!userRepository.existsByUserId(new UserId(userId))) {
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 사용자입니다.");
        }
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }

        PointModel point = pointRepository.findByUserId(new UserId(userId))
                .orElseGet(() -> pointRepository.create(PointModel.builder()
                        .userId(new UserId(userId))
                        .amount(0)
                        .build()
                ));

        point.addPoint(amount);

        return pointRepository.create(point);
    }

}
