package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_history")
@Getter
public class PaymentHistoryModel extends BaseEntity {

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private int amount;
    private LocalDateTime paymentDate;

    protected PaymentHistoryModel() {}

    public static PaymentHistoryModel complete(Long orderId, PaymentMethod paymentMethod, int amount) {
        PaymentHistoryModel payment = new PaymentHistoryModel();
        payment.orderId = orderId;
        payment.paymentMethod = paymentMethod;
        payment.paymentStatus = PaymentStatus.SUCCESS;
        payment.amount = amount;
        payment.paymentDate = LocalDateTime.now();
        return payment;
    }

    public void success() {
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    public void fail() {
        this.paymentStatus = PaymentStatus.FAILED;
    }
} 
