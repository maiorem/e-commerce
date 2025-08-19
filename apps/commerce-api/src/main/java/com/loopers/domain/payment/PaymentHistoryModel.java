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

    private String transactionKey;

    private int finalOrderPrice;

    private LocalDateTime paymentDate;

    protected PaymentHistoryModel() {}

    public static PaymentHistoryModel of(Long orderId, PaymentMethod paymentMethod, int finalOrderPrice, PaymentResult result) {
        PaymentHistoryModel payment = new PaymentHistoryModel();
        payment.orderId = orderId;
        payment.paymentMethod = paymentMethod;
        payment.paymentStatus = result.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        payment.transactionKey = result.transactionKey();
        payment.finalOrderPrice = finalOrderPrice;
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
