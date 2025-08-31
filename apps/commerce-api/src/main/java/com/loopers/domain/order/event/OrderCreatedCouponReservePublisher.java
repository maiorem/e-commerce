package com.loopers.domain.order.event;

public interface OrderCreatedCouponReservePublisher {
    void publish(OrderCeatedCouponReserveCommand command);
}
