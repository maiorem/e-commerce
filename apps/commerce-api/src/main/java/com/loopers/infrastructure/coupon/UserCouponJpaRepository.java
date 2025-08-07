package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.UserCouponModel;
import com.loopers.domain.user.UserId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface UserCouponJpaRepository extends JpaRepository<UserCouponModel, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserCouponModel> findByUserIdAndCouponCode(UserId userId, String couponCode);
    
}
