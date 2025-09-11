package com.loopers.event;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
public class OrderCreatedEvent extends BaseEvent {

    private final Long orderId;
    private final String orderNumber;
    private final String userId;
    private final int totalAmount;
    private final LocalDateTime orderDate;
    private final List<OrderItemInfo> orderItems;

    public OrderCreatedEvent(String eventId, Long orderId, String orderNumber, String userId,
                           int totalAmount, LocalDateTime orderDate, List<OrderItemInfo> orderItems, ZonedDateTime occurredAt) {
        super(eventId, occurredAt);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
        this.orderItems = orderItems;
    }

    @Getter
    public static class OrderItemInfo {
        private final Long productId;
        private final String productName;
        private final int price;
        private final int quantity;
        private final int itemTotalAmount;

        public OrderItemInfo(Long productId, String productName, int price, int quantity, int itemTotalAmount) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.itemTotalAmount = itemTotalAmount;
        }
    }
}