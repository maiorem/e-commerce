package com.loopers.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
public class OrderDate {

    @Column(name = "order_date")
    private LocalDateTime value;

    protected OrderDate() {}
    
    public static OrderDate of(LocalDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("주문 날짜는 비어있을 수 없습니다.");
        }

        if (value.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("주문 날짜는 미래일 수 없습니다.");
        }

        OrderDate orderDate = new OrderDate();
        orderDate.value = value;
        return orderDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDate orderDate = (OrderDate) o;
        return Objects.equals(value, orderDate.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
} 
