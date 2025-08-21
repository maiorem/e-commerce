package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.order.Money;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentModel extends BaseEntity {

    private Long orderId;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String transactionKey;

    private Money finalOrderPrice;

    private LocalDateTime paymentDate;

    public static PaymentModel create(Long orderId, PaymentMethod paymentMethod, Money finalOrderPrice, PaymentResult result) {
        PaymentModel payment = new PaymentModel();
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
