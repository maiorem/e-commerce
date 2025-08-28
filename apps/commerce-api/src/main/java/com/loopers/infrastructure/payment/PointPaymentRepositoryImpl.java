package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.model.PointPayment;
import com.loopers.domain.payment.repository.PointPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointPaymentRepositoryImpl implements PointPaymentRepository {

    private final PointPaymentJpaRepository pointPaymentJpaRepository;

    @Override
    public PointPayment save(PointPayment pointPayment) {
        return pointPaymentJpaRepository.save(pointPayment);
    }
}
