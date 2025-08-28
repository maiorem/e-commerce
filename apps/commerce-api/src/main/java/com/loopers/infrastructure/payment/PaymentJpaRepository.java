package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.model.PaymentModel;
import com.loopers.domain.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface PaymentJpaRepository extends JpaRepository<PaymentModel, Long> {
    List<PaymentModel> findByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, ZonedDateTime createdAt);
}
