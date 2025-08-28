package com.loopers.application.order;

import com.loopers.domain.order.Money;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Getter
@Builder
public class OrderPricing {
    private Money originalAmount;
    private Money couponDiscount;
    private Money finalAmount;
    @Nullable
    private String couponCode;
}
