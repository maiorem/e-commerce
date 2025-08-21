package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public PaymentModel save(PaymentModel payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentModel> findById(Long id) {
        return paymentJpaRepository.findById(id);
    }

    @Override
    public List<PaymentModel> findAll() {
        return paymentJpaRepository.findAll();
    }

    @Override
    public Optional<PaymentModel> findByTransactionKey(String transactionKey) {
        return paymentJpaRepository.findByTransactionKey(transactionKey);
    }

    @Override
    public List<PaymentModel> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, LocalDateTime time) {
        return paymentJpaRepository.findByStatusAndCreatedAtBefore(paymentStatus, time);
    }
}
