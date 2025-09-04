package com.loopers.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class StockAdjustedEvent {

    private final Long productId;
    private final int oldStock;
    private final int newStock;
    private final ZonedDateTime occurredAt;
    private final String eventId;

    public static StockAdjustedEvent create(Long productId, int oldStock, int newStock) {
        return new StockAdjustedEvent(
                productId,
                oldStock,
                newStock,
                ZonedDateTime.now(),
                "stock-adjusted-" + productId + "-" + System.currentTimeMillis()
        );
    }

    public int getStockDelta() {
        return newStock - oldStock;
    }

    public boolean isStockDepleted() {
        return newStock <= 0;
    }

}
