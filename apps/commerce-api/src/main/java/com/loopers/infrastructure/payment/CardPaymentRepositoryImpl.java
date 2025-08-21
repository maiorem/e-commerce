package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.CardPayment;
import com.loopers.domain.payment.CardPaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CardPaymentRepositoryImpl implements CardPaymentRepository {

    private final CardPaymentJpaRepository cardPaymentJpaRepository;

    @Override
    public CardPayment save(CardPayment cardPayment) {
        return cardPaymentJpaRepository.save(cardPayment);
    }

    @Override
    public List<CardPayment> findByStatusAndCreatedBefore(PaymentStatus paymentStatus, LocalDateTime localDateTime) {
        return cardPaymentJpaRepository.findByStatusAndCreatedBefore(paymentStatus, localDateTime);
    }
}
