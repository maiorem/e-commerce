package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.model.PaymentModel;
import com.loopers.domain.payment.repository.PaymentRepository;
import com.loopers.domain.payment.model.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
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
    public List<PaymentModel> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, ZonedDateTime localDateTime) {
        return paymentJpaRepository.findByPaymentStatusAndCreatedAtBefore(paymentStatus, localDateTime);
    }
}
