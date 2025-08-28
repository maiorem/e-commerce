package com.loopers.domain.order.event;

import com.loopers.domain.user.UserId;

public record OrderCeatedCouponReserveCommand(
        Long orderId, UserId userId, String couponCode
) {
    public static OrderCeatedCouponReserveCommand create(Long orderId, UserId userId, String couponCode){
        return new OrderCeatedCouponReserveCommand(orderId, userId, couponCode);
    }
}
