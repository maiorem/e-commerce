package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PointPayment;
import com.loopers.domain.payment.PointPaymentRepository;

public class PointPaymentRepositoryImpl implements PointPaymentRepository {

    private PointPaymentJpaRepository pointPaymentJpaRepository;

    @Override
    public PointPayment save(PointPayment pointPayment) {
        return pointPaymentJpaRepository.save(pointPayment);
    }
}
