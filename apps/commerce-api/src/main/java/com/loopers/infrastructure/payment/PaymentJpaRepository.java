package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<PaymentHistoryModel, Long> {
}
