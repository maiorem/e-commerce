package com.loopers.domain.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderPricing {
    private final Money originalAmount;       // 쿠폰/포인트 이전 총액
    private final Money couponDiscount;       // 쿠폰으로 절감된 금액
    private final Money finalAmount;          // 최종 결제 금액
    private final String couponCode;          // 감사/추적 목적
}


