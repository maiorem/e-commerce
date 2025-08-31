package com.loopers.interfaces.api.event;

import com.loopers.domain.product.event.ClickContext;

public class EventV1Dto {


    public record ProductClickRequest(Long productId, ClickContext context) {

        public EventV1Dto.ProductClickRequest create() {
            return new EventV1Dto.ProductClickRequest(productId, context);
        }
    }

}
