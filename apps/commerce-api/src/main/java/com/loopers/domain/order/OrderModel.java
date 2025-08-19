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

    private String transactionKey;
    private String couponCode;
    private int usedPoints;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    protected OrderModel() {}

    public static OrderModel create(UserId userId, int totalAmount, String couponCode, int usedPoints) {
        OrderModel order = new OrderModel();
        order.userId = userId;
        order.orderNumber = OrderNumberGenerator.generateOrderNumber();
        order.orderDate = OrderDate.of(LocalDateTime.now());
        order.totalAmount = totalAmount;
        order.couponCode = couponCode;
        order.usedPoints = usedPoints;
        order.status = OrderStatus.CREATED;
        return order;
    }

    public void pending(String transactionKey) {
        if (transactionKey == null || transactionKey.isEmpty()) {
            throw new IllegalArgumentException("트랜잭션 키는 비어있을 수 없습니다.");
        }
        this.transactionKey = transactionKey;
        this.status = OrderStatus.PENDING;
    }

    public void confirmPayment() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancelByPaymentFailure() {
        this.status = OrderStatus.CANCELLED;
    }
} 
