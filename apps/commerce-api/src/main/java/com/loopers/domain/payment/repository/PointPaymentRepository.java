package com.loopers.domain.payment.repository;

import com.loopers.domain.payment.model.PointPayment;
import org.springframework.stereotype.Repository;

@Repository
public interface PointPaymentRepository {
    PointPayment save(PointPayment pointPayment);
}
