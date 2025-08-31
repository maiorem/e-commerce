package com.loopers.domain.product.event;

public interface StockAdjustedPublisher {
    void publish(StockAdjustedEvent event);
}
