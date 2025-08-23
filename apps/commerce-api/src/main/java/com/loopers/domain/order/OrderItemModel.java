package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "order_item")
@Getter
public class OrderItemModel extends BaseEntity {

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "product_id")
    private Long productId;

    private int quantity;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "price_at_order"))
    private Money priceAtOrder;

    protected OrderItemModel() {}

    @Builder
    public OrderItemModel(Long orderId, Long productId, int quantity, Money priceAtOrder) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public void assignToOrder(Long orderId) {
        this.orderId = orderId;
    }
    
    public Money calculateTotalPrice() {
        return priceAtOrder.multiply(quantity);
    }
    
    public Long getOrderId() {
        return this.orderId;
    }
} 
