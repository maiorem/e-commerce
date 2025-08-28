package com.loopers.domain.order.event;

import com.loopers.domain.order.Money;
import com.loopers.domain.order.OrderModel;
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
    private final LocalDateTime orderDate;
    private final ZonedDateTime occurredAt;


    public static OrderCreatedEvent from(OrderModel order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getOrderNumber().getValue(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getOrderDate().getValue(),
                ZonedDateTime.now()
        );
    }
}
