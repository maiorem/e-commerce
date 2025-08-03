package com.loopers.application.order;

import com.loopers.domain.user.UserId;

import java.util.List;

public record OrderCommand(
        UserId userId,
        int usePoints,
        List<OrderItemCommand> items
        ) {
}
