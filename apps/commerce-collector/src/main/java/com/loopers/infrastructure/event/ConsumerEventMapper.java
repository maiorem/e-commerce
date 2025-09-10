package com.loopers.infrastructure.event;

import com.loopers.event.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

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
        return new OrderCreatedEvent(
                (String) eventData.get("eventId"),
                (Long) eventData.get("orderId"),
                (String) eventData.get("orderNumber"),
                (String) eventData.get("userId"),
                (Integer) eventData.get("totalAmount"),
                (LocalDateTime) eventData.get("orderDate"),
                (ZonedDateTime) eventData.get("occurredAt")
        );
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