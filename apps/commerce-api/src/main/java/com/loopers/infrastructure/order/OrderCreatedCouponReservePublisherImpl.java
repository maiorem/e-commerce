package com.loopers.infrastructure.order;

import com.loopers.domain.order.event.OrderCeatedCouponReserveCommand;
import com.loopers.domain.order.event.OrderCreatedCouponReservePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedCouponReservePublisherImpl implements OrderCreatedCouponReservePublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OrderCeatedCouponReserveCommand command) {
        applicationEventPublisher.publishEvent(command);
    }
}
