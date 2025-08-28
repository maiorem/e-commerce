package com.loopers.infrastructure.order;

import com.loopers.domain.order.event.OrderCreatedStockDeductionCommand;
import com.loopers.domain.order.event.OrderCreatedStockDeductionPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedStockDeductionPublisherImpl implements OrderCreatedStockDeductionPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(OrderCreatedStockDeductionCommand command) {
        eventPublisher.publishEvent(command);
    }
}
