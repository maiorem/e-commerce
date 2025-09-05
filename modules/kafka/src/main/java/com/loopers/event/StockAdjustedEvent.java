package com.loopers.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class StockAdjustedEvent {

    private final String eventId;
    private final Long productId;
    private final int oldStock;
    private final int newStock;
    private final ZonedDateTime occurredAt;

    public static StockAdjustedEvent create(Long productId, int oldStock, int newStock) {
        ZonedDateTime now = ZonedDateTime.now();
        String eventId = "stock-adjusted-" + productId + "-" + now.toInstant().toEpochMilli();

        return new StockAdjustedEvent(
                eventId,
                productId,
                oldStock,
                newStock,
                now
        );
    }

    public static StockAdjustedEvent createWithContext(
            Long productId,
            int oldStock,
            int newStock) {
        ZonedDateTime now = ZonedDateTime.now();
        String eventId = "stock-adjusted-" + productId + "-" + now.toInstant().toEpochMilli();

        return new StockAdjustedEvent(
                eventId,
                productId,
                oldStock,
                newStock,
                now
        );
    }

    public int getStockDelta() {
        return newStock - oldStock;
    }

    public boolean isStockDepleted() {
        return newStock <= 0;
    }

    public boolean isStockIncreased() {
        return newStock > oldStock;
    }

    /**
     * 다른 이벤트보다 최신인지 확인
     */
    public boolean isNewerThan(StockAdjustedEvent other) {
        return this.occurredAt.isAfter(other.occurredAt);
    }

    @Override
    public String toString() {
        return String.format("StockAdjustedEvent{productId=%d, %d→%d, at=%s}",
                productId, oldStock, newStock, occurredAt);
    }
}
