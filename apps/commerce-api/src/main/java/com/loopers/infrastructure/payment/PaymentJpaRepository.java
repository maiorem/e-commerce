package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentModel, Long> {
    List<PaymentModel> findByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, ZonedDateTime createdAt);
}
