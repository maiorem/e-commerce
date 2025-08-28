package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.coupon.UserCouponRepository;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {

    private final UserCouponJpaRepository userCouponJpaRepository;

    @Override
    public Optional<UserCouponModel> findByUserIdAndCouponCode(UserId userId, String couponCode) {
        return userCouponJpaRepository.findByUserIdAndCouponCode(userId, couponCode);
    }

    @Override
    public UserCouponModel save(UserCouponModel userCoupon) {
        return userCouponJpaRepository.save(userCoupon);
    }

    @Override
    public Optional<UserCouponModel> findByOrderId(Long orderId) {
        return userCouponJpaRepository.findByOrderId(orderId);
    }
}
