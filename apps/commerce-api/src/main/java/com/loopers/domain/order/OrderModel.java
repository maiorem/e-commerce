package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
public class OrderModel extends BaseEntity {

    @Embedded
    private UserId userId;

    @Embedded
    private OrderDate orderDate;

    @Embedded
    private OrderNumber orderNumber;

    private int totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected OrderModel() {}

    public static OrderModel of(UserId userId, int totalAmount) {
        OrderModel order = new OrderModel();
        order.userId = userId;
        order.orderNumber = OrderNumberGenerator.generateOrderNumber();
        order.orderDate = OrderDate.of(LocalDateTime.now());
        order.totalAmount = totalAmount;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
} 
