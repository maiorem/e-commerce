package com.loopers.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
public class OrderCreatedEvent extends BaseEvent {

    private final Long orderId;
    private final String orderNumber;
    private final String userId;
    private final int totalAmount;
    private final LocalDateTime orderDate;

    public OrderCreatedEvent(String eventId, Long orderId, String orderNumber, String userId,
                           int totalAmount, LocalDateTime orderDate, ZonedDateTime occurredAt) {
        super(eventId, occurredAt);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }
}