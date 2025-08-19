package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    @PostMapping("/callback")
    public ApiResponse<Void> handlePaymnetCallback(PaymentV1Dto.PaymentCallbackResponse response) {

        paymentApplicationService.handlePaymentCallback(response.transactionId(), response.status(), response.reason());




        return null;
    }
}
