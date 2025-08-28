package com.loopers.domain.order.event;

import com.loopers.domain.order.OrderItemModel;

import java.util.List;

public record OrderCreatedStockDeductionCommand(
    List<OrderItemModel> orderItemList
) {
    public static OrderCreatedStockDeductionCommand create(
            List<OrderItemModel> orderItemList
    ){
        return new OrderCreatedStockDeductionCommand(orderItemList);
    }
}
