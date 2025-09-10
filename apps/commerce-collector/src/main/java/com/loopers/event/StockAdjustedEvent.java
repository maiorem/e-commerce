package com.loopers.event;

import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class StockAdjustedEvent extends BaseEvent {

    private final Long productId;
    private final int oldStock;
    private final int newStock;
    private final int adjustmentAmount;
    private final String adjustmentReason;

    public StockAdjustedEvent(String eventId, Long productId, int oldStock, int newStock,
                            int adjustmentAmount, String adjustmentReason, ZonedDateTime occurredAt) {
        super(eventId, occurredAt);
        this.productId = productId;
        this.oldStock = oldStock;
        this.newStock = newStock;
        this.adjustmentAmount = adjustmentAmount;
        this.adjustmentReason = adjustmentReason;
    }
}