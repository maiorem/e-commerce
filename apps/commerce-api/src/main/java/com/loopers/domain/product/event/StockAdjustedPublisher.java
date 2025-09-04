package com.loopers.domain.product.event;

import com.loopers.event.StockAdjustedEvent;

public interface StockAdjustedPublisher {
    void publish(StockAdjustedEvent event);
}
