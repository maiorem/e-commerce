package com.loopers.domain.payment.repository;

import com.loopers.domain.payment.model.CardPayment;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardPaymentRepository {
    CardPayment save(CardPayment cardPayment);

    Optional<CardPayment> findByTransactionKey(String transactionId);

    Optional<CardPayment> findByPaymentId(Long paymentId);
}
