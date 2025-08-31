package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.model.PointPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointPaymentJpaRepository extends JpaRepository<PointPayment, Long> {
}
