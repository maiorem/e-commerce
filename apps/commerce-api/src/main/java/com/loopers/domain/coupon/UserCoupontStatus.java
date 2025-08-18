package com.loopers.domain.coupon;

public enum UserCoupontStatus {
    AVAILABLE,  // 사용 가능
    RESERVED,   // 예약됨 (결제 대기)
    USED,       // 사용 완료
    EXPIRED     // 만료됨
}
