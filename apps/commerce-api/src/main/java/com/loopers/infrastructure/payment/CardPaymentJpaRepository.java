package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPayment;
import com.loopers.domain.payment.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CardPaymentJpaRepository extends JpaRepository<CardPayment, Long> {
    List<CardPayment> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, LocalDateTime localDateTime);
}
