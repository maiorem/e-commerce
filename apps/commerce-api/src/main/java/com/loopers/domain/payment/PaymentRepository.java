package com.loopers.domain.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    PaymentModel save(PaymentModel payment);
    
    Optional<PaymentModel> findById(Long id);

    List<PaymentModel> findAll();

    Optional<PaymentModel> findByTransactionKey(String transactionKey);

    List<PaymentModel> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, LocalDateTime timestamp);
} 
