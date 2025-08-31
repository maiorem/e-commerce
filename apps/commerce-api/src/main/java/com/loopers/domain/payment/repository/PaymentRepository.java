package com.loopers.domain.payment.repository;

import com.loopers.domain.payment.model.PaymentModel;
import com.loopers.domain.payment.model.PaymentStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    PaymentModel save(PaymentModel payment);
    
    Optional<PaymentModel> findById(Long id);

    List<PaymentModel> findAll();

    List<PaymentModel> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, ZonedDateTime localDateTime);
}
