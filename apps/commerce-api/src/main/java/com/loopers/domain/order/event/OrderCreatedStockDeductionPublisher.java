package com.loopers.domain.order.event;

public interface OrderCreatedStockDeductionPublisher {
    void publish(OrderCreatedStockDeductionCommand command);
}
