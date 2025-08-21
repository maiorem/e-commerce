package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "card_payment")
public class CardPayment extends BaseEntity {

    Long paymentId;

    private String transactionKey;

    @Enumerated(EnumType.STRING)
    private CardType cardType;

    private String cardNumber;

    protected CardPayment() {
    }

    public static CardPayment create(PaymentModel payment, String transactionKey, CardType cardType, String cardNumber) {
        CardPayment cardPayment = new CardPayment();
        cardPayment.paymentId = payment.getId();
        cardPayment.transactionKey = transactionKey;
        cardPayment.cardType = cardType;
        cardPayment.cardNumber = cardNumber;
        return cardPayment;
    }

    public void updateTransactionKey(String transactionKey) {
        this.transactionKey = transactionKey;
    }

    public PaymentData toPaymentData(UserId userId, PaymentModel payment) {
        return PaymentData.create(
                payment.getOrderId(),
                payment.getPaymentMethod(),
                cardType,
                cardNumber,
                payment.getFinalOrderPrice(),
                userId
        );
    }
}
