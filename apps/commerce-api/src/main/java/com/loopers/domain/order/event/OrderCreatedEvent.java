package com.loopers.domain.order.event;

import com.loopers.domain.order.Money;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {

    private final Long orderId;
    private final String orderNumber;
    private final UserId userId;
    private final Money totalAmount;
    private final PaymentMethod paymentMethod;
    private final String couponCode;
    private final CardType cardType;
    private final String cardNumber;
    private final Integer pointAmount;
    private final LocalDateTime orderDate;
    private final ZonedDateTime occurredAt;


    public static OrderCreatedEvent from(OrderModel order, String couponCode, PaymentMethod paymentMethod, CardType cardType, String cardNumber, Integer pointAmount) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber().getValue(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                couponCode,
                cardType,
                cardNumber,
                pointAmount,
                order.getOrderDate().getValue(),
                ZonedDateTime.now()
        );
    }
}
