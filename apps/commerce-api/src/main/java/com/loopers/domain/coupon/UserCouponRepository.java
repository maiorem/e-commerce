package com.loopers.domain.coupon;

import com.loopers.domain.user.UserId;

import java.util.Optional;

public interface UserCouponRepository {
    Optional<UserCouponModel> findByUserIdAndCouponCode(UserId userId, String couponCode);

    UserCouponModel save(UserCouponModel userCoupon);

    Optional<UserCouponModel> findByOrderId(Long orderId);
}
