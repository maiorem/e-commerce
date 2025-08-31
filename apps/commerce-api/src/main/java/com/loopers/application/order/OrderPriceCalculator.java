package com.loopers.application.order;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.domain.order.Money;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderPriceCalculator {

    private final CouponProcessor couponProcessor;

    public OrderPricing calculate(UserId userId, int originalAmount, String couponCode) {
        int discountedByCoupon = couponProcessor.applyCouponDiscount(userId, originalAmount, couponCode);

        return OrderPricing.builder()
                .originalAmount(Money.of(originalAmount))
                .couponDiscount(Money.of(originalAmount - discountedByCoupon))
                .finalAmount(Money.of(discountedByCoupon))
                .couponCode(couponCode)
                .build();
    }
}


