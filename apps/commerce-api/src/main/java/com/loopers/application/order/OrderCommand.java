package com.loopers.application.order;

import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.user.UserId;

import java.util.List;

public record OrderCommand(
        UserId userId,
        PaymentMethod paymentMethod,
        int usePoints,
        List<OrderItemCommand> items
        ) {
}
