package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CouponValidationDomainService {

    public void validateCouponUsage(UserCouponModel userCoupon, CouponModel coupon, int orderPrice, LocalDate now) {
        if (!coupon.isValid(orderPrice, now)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 유효하지 않습니다.");
        }
        if (userCoupon.isUsed()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
    }
}
