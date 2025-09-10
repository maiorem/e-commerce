package com.loopers.infrastructure.event;

import com.loopers.domain.like.event.LikeChangedEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.domain.product.event.StockAdjustedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventMapper {

    public Map<String, Object> toConsumerEvent(ProductViewedEvent domainEvent) {
        Map<String, Object> consumerEvent = new HashMap<>();
        consumerEvent.put("eventId", domainEvent.getEventId());
        consumerEvent.put("productId", domainEvent.getProductId());
        consumerEvent.put("userId", domainEvent.getUserId());
        consumerEvent.put("occurredAt", domainEvent.getOccurredAt());
        return consumerEvent;
    }

    public Map<String, Object> toConsumerEvent(LikeChangedEvent domainEvent) {
        Map<String, Object> consumerEvent = new HashMap<>();
        consumerEvent.put("eventId", domainEvent.getEventId());
        consumerEvent.put("productId", domainEvent.getProductId());
        consumerEvent.put("userId", domainEvent.getUserId());
        consumerEvent.put("changeType", domainEvent.getChangeType().name());
        consumerEvent.put("oldLikeCount", domainEvent.getOldLikeCount());
        consumerEvent.put("newLikeCount", domainEvent.getNewLikeCount());
        consumerEvent.put("version", domainEvent.getVersion());
        consumerEvent.put("occurredAt", domainEvent.getOccurredAt());
        return consumerEvent;
    }

    public Map<String, Object> toConsumerEvent(OrderCreatedEvent domainEvent) {
        Map<String, Object> consumerEvent = new HashMap<>();
        consumerEvent.put("eventId", domainEvent.getEventId());
        consumerEvent.put("orderId", domainEvent.getOrderId());
        consumerEvent.put("orderNumber", domainEvent.getOrderNumber());
        consumerEvent.put("userId", domainEvent.getUserId());
        consumerEvent.put("totalAmount", domainEvent.getTotalAmount());
        consumerEvent.put("orderDate", domainEvent.getOrderDate());
        consumerEvent.put("occurredAt", domainEvent.getOccurredAt());
        return consumerEvent;
    }

    public Map<String, Object> toConsumerEvent(StockAdjustedEvent domainEvent) {
        Map<String, Object> consumerEvent = new HashMap<>();
        consumerEvent.put("eventId", domainEvent.getEventId());
        consumerEvent.put("productId", domainEvent.getProductId());
        consumerEvent.put("oldStock", domainEvent.getOldStock());
        consumerEvent.put("newStock", domainEvent.getNewStock());
        consumerEvent.put("adjustmentAmount", domainEvent.getStockDelta());
        consumerEvent.put("adjustmentReason", domainEvent.isStockIncreased() ? "RESTOCK" : "SOLD");
        consumerEvent.put("occurredAt", domainEvent.getOccurredAt());
        return consumerEvent;
    }
}
