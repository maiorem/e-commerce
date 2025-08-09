package com.loopers.domain.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CouponValidationDomainService {
    
    public void validateCouponUsage(UserCouponModel userCoupon, CouponModel coupon, int orderPrice, LocalDate now) {
        if (!coupon.isValid(orderPrice, now)) {
            throw new IllegalArgumentException("쿠폰이 유효하지 않습니다.");
        }
        if (userCoupon.isUsed()) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }
    }
}
