package com.loopers.domain.payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    PaymentHistoryModel save(PaymentHistoryModel payment);
    
    Optional<PaymentHistoryModel> findById(Long id);
    
    Optional<PaymentHistoryModel> findByOrderId(Long orderId);
    
    List<PaymentHistoryModel> findAll();
    
    void delete(PaymentHistoryModel payment);
} 
