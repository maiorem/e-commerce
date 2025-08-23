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
    public ApiResponse handlePaymentCallback(PaymentV1Dto.PaymentCallbackRequest request) {
        try {
            log.info("결제 콜백 수신 - TransactionKey: {}, Status: {}, Reason: {}",
                    request.transactionKey(), request.status(), request.reason());

            // 콜백 요청 유효성 검증
            paymentApplicationService.validatePaymentCallback(request.transactionKey());

            // 결제 콜백 처리
            paymentApplicationService.handlePaymentCallback(request.transactionKey(), request.status(), request.reason());

            log.info("결제 콜백 처리 완료 - TransactionKey: {}", request.transactionKey());
            return ApiResponse.success("결제 콜백 처리 완료");

        } catch (IllegalArgumentException e) {
            log.error("잘못된 콜백 요청: {}", e.getMessage());
            return ApiResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("콜백 처리 중 오류 발생: transactionKey={}, error={}",
                    request.transactionKey(), e.getMessage(), e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "콜백 처리에 실패했습니다.");
        }
    }
}
