package com.loopers.domain.external;

import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.user.UserId;

import java.time.ZonedDateTime;

public interface DataPlatformPort {

    DataPlatformResult sendOrderData(OrderCreatedEvent event);
    void sendUserActionData(UserId userId, String action, Long targetId, ZonedDateTime timestamp);

}
