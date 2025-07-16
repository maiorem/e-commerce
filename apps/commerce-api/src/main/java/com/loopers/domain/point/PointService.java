package com.loopers.domain.point;

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
       

        return null;
    }

}
