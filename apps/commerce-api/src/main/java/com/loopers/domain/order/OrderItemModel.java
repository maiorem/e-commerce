package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItemModel extends BaseEntity {

    private Long orderId;
    private Long productId;

    private int quantity;
    private int priceAtOrder;

    protected OrderItemModel() {}

    @Builder
    public OrderItemModel(Long orderId, Long productId, int quantity, int priceAtOrder) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
} 
