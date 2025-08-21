package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentApplicationService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payment")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentApplicationService paymentApplicationService;

    @Override
    @PostMapping("/callback")
    public ApiResponse handlePaymnetCallback(PaymentV1Dto.PaymentCallbackResponse response) {
        try {
            // 콜백 요청 유효성 검증
            paymentApplicationService.validatePaymentCallback(response.transactionId());

            paymentApplicationService.handlePaymentCallback(response.transactionId(), response.status(), response.reason());

            return ApiResponse.success();
        } catch (IllegalArgumentException e) {
            log.error("잘못된 콜백 요청: {}", e.getMessage());
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("콜백 처리 중 오류 발생: transactionId={}, error={}",
                    response.transactionId(), e.getMessage(), e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "콜백 처리에 실패했습니다.");
        }
    }
}
