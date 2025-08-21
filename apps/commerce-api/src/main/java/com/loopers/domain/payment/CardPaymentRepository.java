package com.loopers.domain.payment;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CardPaymentRepository {
    CardPayment save(CardPayment cardPayment);

    List<CardPayment> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, LocalDateTime localDateTime);
}
