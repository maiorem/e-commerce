package com.loopers.domain.order;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class OrderNumber {

    @Column(name = "order_number", nullable = false, unique = true)
    private String value;

    protected OrderNumber() {}

    public static OrderNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 비어있을 수 없습니다.");
        }
        OrderNumber orderNumber = new OrderNumber();
        orderNumber.value = value;
        return orderNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderNumber that = (OrderNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
