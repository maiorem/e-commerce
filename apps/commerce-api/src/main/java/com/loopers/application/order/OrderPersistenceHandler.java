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

        if (order.getId() == null) {
            throw new IllegalStateException("주문 ID가 설정되지 않았습니다. 주문을 먼저 저장해주세요.");
        }

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
