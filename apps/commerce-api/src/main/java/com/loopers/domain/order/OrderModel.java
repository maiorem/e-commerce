package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.user.UserId;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private Money totalAmount;

    private String couponCode;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Transient
    private List<OrderItemModel> orderItems = new ArrayList<>();


    protected OrderModel() {}

    public static OrderModel create(UserId userId, Money totalAmount, String couponCode, PaymentMethod paymentMethod) {
        OrderModel order = new OrderModel();
        order.userId = userId;
        order.orderNumber = OrderNumberGenerator.generateOrderNumber();
        order.orderDate = OrderDate.of(LocalDateTime.now());
        order.totalAmount = totalAmount;
        order.couponCode = couponCode;
        order.paymentMethod = paymentMethod;
        order.status = OrderStatus.CREATED;
        return order;
    }

    public void confirm() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("CREATED 상태에서만 확정 가능합니다.");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("이미 확정된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isCancellable() {
        return this.status == OrderStatus.CREATED || this.status == OrderStatus.CONFIRMED;
    }
}
