package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface PaymentV1ApiSpec {

    ApiResponse<Void> handlePaymnetCallback(@RequestBody PaymentV1Dto.PaymentCallbackResponse response);
}
