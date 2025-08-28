package com.loopers.domain.product.event;

import lombok.Getter;

@Getter
public class ProductChangedEvent {

    private final Long productId;
    private final ChangeType changeType;

    public ProductChangedEvent(Long productId, ChangeType changeType) {
        this.productId = productId;
        this.changeType = changeType;
    }

}
