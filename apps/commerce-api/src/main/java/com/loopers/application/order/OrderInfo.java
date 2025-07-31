package com.loopers.application.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderNumber;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.user.UserId;

import java.util.List;

public record OrderInfo(
        Long orderId,
        OrderNumber orderNumber,
        UserId userId,
        String productName,
        int quantity,
        double totalPrice,
        String orderDate,
        OrderStatus status
) {

    public static OrderInfo from(OrderModel orderModel, List<OrderItemInfo> orderItemInfoList) {
        return new OrderInfo(
                orderModel.getId(),
                orderModel.getOrderNumber(),
                orderModel.getUserId(),
                orderItemInfoList.stream().map(OrderItemInfo::productName).findFirst().orElse(""),
                orderItemInfoList.stream().mapToInt(OrderItemInfo::quantity).sum(),
                orderModel.getTotalAmount(), // 포인트 차감이 반영된 실제 결제 금액 사용
                orderModel.getOrderDate().toString(),
                orderModel.getStatus()
        );
    }


}
