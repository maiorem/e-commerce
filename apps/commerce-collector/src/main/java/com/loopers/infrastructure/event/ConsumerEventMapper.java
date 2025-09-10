package com.loopers.infrastructure.event;

import com.loopers.event.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConsumerEventMapper {

    public ProductViewedEvent toProductViewedEvent(Map<String, Object> eventData) {
        return new ProductViewedEvent(
                (String) eventData.get("eventId"),
                (Long) eventData.get("productId"),
                (String) eventData.get("userId"),
                (ZonedDateTime) eventData.get("occurredAt")
        );
    }

    public LikeChangedEvent toLikeChangedEvent(Map<String, Object> eventData) {
        return new LikeChangedEvent(
                (String) eventData.get("eventId"),
                (Long) eventData.get("productId"),
                (String) eventData.get("userId"),
                (String) eventData.get("changeType"),
                (Integer) eventData.get("oldLikeCount"),
                (Integer) eventData.get("newLikeCount"),
                (Integer) eventData.get("version"),
                (ZonedDateTime) eventData.get("occurredAt")
        );
    }

    public OrderCreatedEvent toOrderCreatedEvent(Map<String, Object> eventData) {
        // OrderItems 변환
        List<Map<String, Object>> orderItemsData = (List<Map<String, Object>>) eventData.get("orderItems");
        List<OrderCreatedEvent.OrderItemInfo> orderItems = orderItemsData.stream()
                .map(itemData -> new OrderCreatedEvent.OrderItemInfo(
                        getLongValue(itemData.get("productId")),
                        (String) itemData.get("productName"),
                        getIntValue(itemData.get("price")),
                        getIntValue(itemData.get("quantity")),
                        getIntValue(itemData.get("itemTotalAmount"))
                ))
                .collect(Collectors.toList());

        return new OrderCreatedEvent(
                (String) eventData.get("eventId"),
                getLongValue(eventData.get("orderId")),
                (String) eventData.get("orderNumber"),
                (String) eventData.get("userId"),
                getIntValue(eventData.get("totalAmount")),
                (LocalDateTime) eventData.get("orderDate"),
                orderItems,
                (ZonedDateTime) eventData.get("occurredAt")
        );
    }

    private Long getLongValue(Object value) {
        if (value == null) return 0L;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Long) return (Long) value;
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private int getIntValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public StockAdjustedEvent toStockAdjustedEvent(Map<String, Object> eventData) {
        return new StockAdjustedEvent(
                (String) eventData.get("eventId"),
                (Long) eventData.get("productId"),
                (Integer) eventData.get("oldStock"),
                (Integer) eventData.get("newStock"),
                (Integer) eventData.get("adjustmentAmount"),
                (String) eventData.get("adjustmentReason"),
                (ZonedDateTime) eventData.get("occurredAt")
        );
    }
}