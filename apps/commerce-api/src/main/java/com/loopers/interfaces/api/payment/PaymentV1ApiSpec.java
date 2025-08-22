package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;

public interface PaymentV1ApiSpec {

    ApiResponse handlePaymentCallback(PaymentV1Dto.PaymentCallbackRequest request);
}
