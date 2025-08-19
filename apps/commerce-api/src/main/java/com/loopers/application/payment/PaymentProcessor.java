package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentData;
import com.loopers.domain.payment.PaymentGatewayPort;
import com.loopers.domain.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentGatewayPort paymentGatewayPort;

    @Async
    @Retryable(maxAttempts = 3)
    public CompletableFuture<PaymentResult> processAsync(PaymentData data) {
        return CompletableFuture.supplyAsync(() -> {
            return paymentGatewayPort.processPayment(data);
        });
    }

}
