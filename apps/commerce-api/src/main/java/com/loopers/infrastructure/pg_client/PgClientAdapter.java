package com.loopers.infrastructure.pg_client;

import com.loopers.domain.payment.*;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PgClientAdapter implements PaymentGatewayPort {

    private final PgClient pgClient;

    @Value("${payment.pg.callback-url}")
    private String callbackUrl;

    @Retry(name = "pgRetry", fallbackMethod = "fallback")
    @Override
    public PaymentResult processPayment(PaymentData paymentData) {
        PgClientDto.PgClientRequest request = PgClientDto.PgClientRequest.from(paymentData, callbackUrl);
        PgClientDto.PgClientResponse response= pgClient.requestPayment(request);
        return PaymentResult.from(response);
    }

    public PaymentResult fallback(PaymentData paymentData, Throwable t) {
        return PaymentResult.from(Result.FAILED, null, "결제에 실패하였습니다.",
                null, PaymentStatus.FAILED, "Fallback due to: " + t.getMessage());

    }
}
