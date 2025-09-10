package com.loopers.domain.external;

import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSuccessEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;

public interface DataPlatformPort {

    DataPlatformResult sendOrderData(OrderCreatedEvent event);
    DataPlatformResult sendPaymentSuccess(PaymentSuccessEvent event);
    DataPlatformResult sendPaymentFailure(PaymentFailedEvent event);
}
