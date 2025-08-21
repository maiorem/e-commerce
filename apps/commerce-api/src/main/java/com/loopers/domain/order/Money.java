package com.loopers.domain.order;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

/**
 * 금액을 나타내는 값 객체
 */
@Embeddable
@Getter
public class Money {
    
    private final int amount;
    
    protected Money() {
        this.amount = 0;
    }
    
    public Money(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + amount);
        }
        this.amount = amount;
    }
    
    public static Money of(int amount) {
        return new Money(amount);
    }
    
    public static Money zero() {
        return new Money(0);
    }
    
    public Money add(Money other) {
        return new Money(this.amount + other.amount);
    }
    
    public Money subtract(Money other) {
        if (this.amount < other.amount) {
            throw new IllegalArgumentException("차감할 금액이 현재 금액보다 큽니다");
        }
        return new Money(this.amount - other.amount);
    }
    
    public Money multiply(int multiplier) {
        return new Money(this.amount * multiplier);
    }
    
    public boolean isZero() {
        return this.amount == 0;
    }
    
    public boolean isPositive() {
        return this.amount > 0;
    }
    
    public boolean isGreaterThan(Money other) {
        return this.amount > other.amount;
    }
    
    public boolean isGreaterThanOrEqual(Money other) {
        return this.amount >= other.amount;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Money money = (Money) obj;
        return amount == money.amount;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
    
    @Override
    public String toString() {
        return "Money{amount=" + amount + "}";
    }
}