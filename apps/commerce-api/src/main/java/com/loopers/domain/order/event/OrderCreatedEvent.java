package com.loopers.domain.order.event;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.product.ProductModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderCreatedEvent {

    private final String eventId;
    private final Long orderId;
    private final String orderNumber;
    private final String userId;
    private final int totalAmount;
    private final LocalDateTime orderDate;
    private final ZonedDateTime occurredAt;
    private final List<OrderItemInfo> orderItems;


    public static OrderCreatedEvent from(Long orderId, String orderNumber, String userId, int totalAmount, LocalDateTime orderDate, List<OrderItemInfo> orderItems) {
        return new OrderCreatedEvent(
                "order-created-" + orderId + "-" + System.currentTimeMillis(),
                orderId,
                orderNumber,
                userId,
                totalAmount,
                orderDate,
                ZonedDateTime.now(),
                orderItems
        );
    }

    public static OrderCreatedEvent from(Long orderId, String orderNumber, String userId, int totalAmount, LocalDateTime orderDate) {
        return from(orderId, orderNumber, userId, totalAmount, orderDate, List.of());
    }

    public static OrderCreatedEvent from(Long orderId, String orderNumber, String userId, int totalAmount, 
                                        LocalDateTime orderDate, List<OrderItemModel> orderItems, List<ProductModel> products) {
        List<OrderItemInfo> orderItemInfos = orderItems.stream()
                .map(item -> {
                    String productName = products.stream()
                            .filter(p -> p.getId().equals(item.getProductId()))
                            .map(ProductModel::getName)
                            .findFirst()
                            .orElse("Unknown Product");
                    
                    return OrderItemInfo.of(
                            item.getProductId(),
                            productName,
                            item.getPriceAtOrder().getAmount(),
                            item.getQuantity()
                    );
                })
                .toList();
        
        return from(orderId, orderNumber, userId, totalAmount, orderDate, orderItemInfos);
    }

    @Getter
    @RequiredArgsConstructor
    public static class OrderItemInfo {
        private final Long productId;
        private final String productName;
        private final int price;
        private final int quantity;
        private final int itemTotalAmount;

        public static OrderItemInfo of(Long productId, String productName, int price, int quantity) {
            return new OrderItemInfo(productId, productName, price, quantity, price * quantity);
        }
    }
}
