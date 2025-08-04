package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderPersistenceHandler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    public OrderModel saveOrder(OrderModel order) {
       return orderRepository.save(order);
    }

    public List<OrderItemModel> saveOrderItemAndPaymentHistory(OrderModel order, List<OrderItemModel> orderItems, PaymentHistoryModel paymentHistory) {

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

        // 결제 내역 저장
        paymentRepository.save(paymentHistory);

        return savedOrderItems;
    }
}
