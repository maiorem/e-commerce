package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderPersistenceHandler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderModel saveOrder(OrderModel order) {
       return orderRepository.save(order);
    }

    public List<OrderItemModel> saveOrderItem(OrderModel order, List<OrderItemModel> orderItems) {

        List<OrderItemModel> savedOrderItems = new ArrayList<>();
        orderItems.forEach(item -> {
            OrderItemModel orderItemWithOrderId = OrderItemModel.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .priceAtOrder(item.getPriceAtOrder())
                    .build();
            OrderItemModel savedItem = orderItemRepository.save(orderItemWithOrderId);

            savedOrderItems.add(savedItem);
        });

        return savedOrderItems;
    }
}
