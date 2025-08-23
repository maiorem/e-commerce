package com.loopers.application.coupon;

import com.loopers.domain.coupon.*;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class CouponProcessor {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponValidationDomainService couponValidationDomainService;

    public int applyCouponDiscount(UserId userId, int orderPrice, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return orderPrice;
        }

        CouponModel coupon = couponRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰입니다."));

        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)
                .orElseThrow(() -> new IllegalArgumentException("사용자에게 해당 쿠폰이 없습니다."));

        couponValidationDomainService.validateCouponUsage(userCoupon, coupon, orderPrice, LocalDate.now());

        int couponDiscountAmount = coupon.calculateDiscount(orderPrice);

        if (couponDiscountAmount > orderPrice) {
            throw new IllegalArgumentException("쿠폰 할인 금액이 주문 총액을 초과할 수 없습니다.");
        }

        orderPrice -= couponDiscountAmount;
        return orderPrice;
    }

    public void useCoupon(UserId userId, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return;
        }

        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)
                .orElseThrow(() -> new IllegalArgumentException("사용자에게 해당 쿠폰이 없습니다."));
        userCoupon.useCoupon(LocalDate.now());
        userCouponRepository.save(userCoupon);
    }

    public void reserveCoupon(UserId userId, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return;
        }
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)
                .orElseThrow(() -> new IllegalArgumentException("사용자에게 해당 쿠폰이 없습니다."));

        if (userCoupon.getStatus() != UserCoupontStatus.AVAILABLE) {
            throw new IllegalArgumentException("쿠폰이 사용 가능 상태가 아닙니다. 현재 상태: " + userCoupon.getStatus());
        }
        userCoupon.reserve();
        userCouponRepository.save(userCoupon);
    }

    public void restoreCoupon(UserId userId, String couponCode) {
        if (couponCode == null || couponCode.isEmpty()) {
            return;
        }
        UserCouponModel userCoupon = userCouponRepository.findByUserIdAndCouponCode(userId, couponCode)
                .orElseThrow(() -> new IllegalArgumentException("사용자에게 해당 쿠폰이 없습니다."));
        userCoupon.cancelReservation();
        userCouponRepository.save(userCoupon);
    }
}
