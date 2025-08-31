package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.model.CardPayment;
import com.loopers.domain.payment.repository.CardPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CardPaymentRepositoryImpl implements CardPaymentRepository {

    private final CardPaymentJpaRepository cardPaymentJpaRepository;

    @Override
    public CardPayment save(CardPayment cardPayment) {
        return cardPaymentJpaRepository.save(cardPayment);
    }

    @Override
    public Optional<CardPayment> findByTransactionKey(String transactionId) {
        return cardPaymentJpaRepository.findByTransactionKey(transactionId);
    }

    @Override
    public Optional<CardPayment> findByPaymentId(Long paymentId) {
        return cardPaymentJpaRepository.findByPaymentId(paymentId);
    }
}
