package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentHistoryJpaRepository;

    @Override
    public PaymentHistoryModel save(PaymentHistoryModel payment) {
        return null;
    }

    @Override
    public Optional<PaymentHistoryModel> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<PaymentHistoryModel> findByOrderId(Long orderId) {
        return Optional.empty();
    }

    @Override
    public List<PaymentHistoryModel> findAll() {
        return List.of();
    }

    @Override
    public void delete(PaymentHistoryModel payment) {

    }
}
