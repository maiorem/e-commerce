package com.loopers.domain.payment;

import org.springframework.stereotype.Repository;

@Repository
public interface PointPaymentRepository {
    PointPayment save(PointPayment pointPayment);
}
