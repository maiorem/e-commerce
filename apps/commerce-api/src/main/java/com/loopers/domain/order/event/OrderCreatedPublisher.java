package com.loopers.domain.order.event;

import com.loopers.event.OrderCreatedEvent;

public interface OrderCreatedPublisher {
    void  publish(OrderCreatedEvent event);
}
