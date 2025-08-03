package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;

import java.util.List;

public record OrderItemCommand(
    Long orderId,
    Long productId,
    int quantity,
    String productName,
    int productPrice
) {

    public OrderItemCommand create(
            Long orderId,
            Long productId,
            Integer quantity,
            String productName,
            int productPrice
    ) {
        return new OrderItemCommand(orderId, productId, quantity, productName, productPrice);
    }

    public static List<OrderItemModel> convertToOrderItems(List<OrderItemCommand> items) {
        return items.stream()
            .map(OrderItemCommand::convertToOrderItem)
            .toList();
    }


    private static OrderItemModel convertToOrderItem(OrderItemCommand item) {
        return new OrderItemModel(
            item.orderId(),
            item.productId(),
            item.quantity(),
            item.productPrice()
        );
    }
}
