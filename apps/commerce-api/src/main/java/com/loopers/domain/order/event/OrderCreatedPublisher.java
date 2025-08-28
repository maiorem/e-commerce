package com.loopers.domain.order.event;

public interface OrderCreatedPublisher {
    void  publish(OrderCreatedEvent event);
}
