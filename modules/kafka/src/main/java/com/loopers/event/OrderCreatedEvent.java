package com.loopers.event;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {

    private final String eventId;
    private final Long orderId;
    private final String orderNumber;
    private final String userId;
    private final int totalAmount;
    private final LocalDateTime orderDate;
    private final ZonedDateTime occurredAt;


    public static OrderCreatedEvent from(Long orderId, String orderNumber, String userId, int totalAmount, LocalDateTime orderDate) {
        return new OrderCreatedEvent(
                "order-created-" + orderId + "-" + System.currentTimeMillis(),
                orderId,
                orderNumber,
                userId,
                totalAmount,
                orderDate,
                ZonedDateTime.now()
        );
    }
}
