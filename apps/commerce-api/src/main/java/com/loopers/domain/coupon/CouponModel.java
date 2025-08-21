package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponModel extends BaseEntity {

    private String name;
    private String couponCode;
    @Enumerated(EnumType.STRING)
    private CouponType type;
    @Enumerated(EnumType.STRING)
    private CouponStatus status;
    private int discountValue;
    private int minimumOrderAmount;
    private int maximumDiscountAmount;
    private LocalDate issuedAt;
    private LocalDate validUntil;

    @Builder
    public CouponModel(String name, CouponType type, int discountValue, int minimumOrderAmount,
                                     int maximumDiscountAmount, LocalDate issuedAt, LocalDate validUntil) {
        if (name == null || type == null || discountValue < 0 || minimumOrderAmount < 0 || maximumDiscountAmount < 0) {
            throw new IllegalArgumentException("쿠폰 정보가 올바르지 않습니다.");
        }
        this.name = name;
        this.couponCode = CouponCodeGenerator.generateCouponCode();
        this.type = type;
        this.status = CouponStatus.ACTIVE;
        this.discountValue = discountValue;
        this.minimumOrderAmount = minimumOrderAmount;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.issuedAt = issuedAt;
        this.validUntil = validUntil;
    }

    public int calculateDiscount(int orderPrice){
        if (orderPrice < minimumOrderAmount) {
            return 0; // 최소 주문 금액 미달 시 할인 없음
        }
        int discount = 0;
        if (type == CouponType.FIXED_AMOUNT) {
            discount = Math.min(discountValue, maximumDiscountAmount);
        } else if (type == CouponType.PERCENTAGE) {
            discount = (int) Math.min(orderPrice * (discountValue / 100.0), maximumDiscountAmount);
        }
        return discount;

    }
    public boolean isValid(int orderPrice, LocalDate orderDate){
        if (status != CouponStatus.ACTIVE) {
            return false;
        }
        if (orderPrice < minimumOrderAmount) {
            return false;
        }
        if (validUntil != null && orderDate.isAfter(validUntil)) {
            return false;
        }
        return true;

    }

}
