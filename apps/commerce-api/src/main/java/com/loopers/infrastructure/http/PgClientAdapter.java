package com.loopers.infrastructure.http;

import com.loopers.domain.payment.*;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PgClientAdapter implements PaymentGatewayPort {

    private final PgClient pgClient;

    @Value("${payment.pg.callback-url}")
    private String callbackUrl;

    @Override
    @Retry(name = "pgRetry", fallbackMethod = "retryFallback")
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "circuitFallback")
    public PaymentResult processPayment(PaymentData paymentData) {
        log.info("=== PG 결제 processPayment 메서드 호출됨 ===");
        try {
            String userId = paymentData.userId().getValue();
            PgClientDto.PgClientRequest request = PgClientDto.PgClientRequest.from(paymentData, callbackUrl);

            log.info("PG 결제 요청 시작 - OrderId: {}, Amount: {}, UserId: {}",
                    paymentData.orderId(), paymentData.finalTotalPrice().getAmount(), userId);

            PgClientDto.PgClientResponse response = pgClient.requestPayment(userId, request);

            if ("SUCCESS".equals(response.meta().result())) {
                log.info("PG 결제 요청 성공 - OrderId: {}, TransactionKey: {}",
                        paymentData.orderId(), response.data().transactionKey());
                return PaymentResult.success(response.data().transactionKey());
            } else {
                log.error("PG 결제 요청 실패 - OrderId: {}, Error: {}",
                        paymentData.orderId(), response.meta().message());
                return PaymentResult.failed("PG 결제 요청에 실패했습니다: " + response.meta().message());
            }
        } catch (FeignException e) {
            log.error("Feign 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("기타 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("PG 결제 요청 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public PaymentResult retryFallback(PaymentData paymentData, Throwable t) {
        log.warn("Retry fallback 호출됨 - OrderId: {}, Error: {}",
                paymentData.orderId(), t.getMessage());
        return PaymentResult.failed("재시도 후 결제에 실패하였습니다: " + t.getMessage());
    }

    public PaymentResult circuitFallback(PaymentData paymentData, Throwable t) {
        log.warn("CircuitBreaker fallback 호출됨 - OrderId: {}, Error: {}",
                paymentData.orderId(), t.getMessage());
        return PaymentResult.failed("서킷 브레이커로 인해 결제에 실패하였습니다: " + t.getMessage());
    }

    @Override
    public PaymentQueryResult queryPaymentStatus(String userId, String transactionKey) {
        try {
            PgClientDto.PgClientQueryResponse response = pgClient.getTransaction(userId, transactionKey);

            return PaymentQueryResult.success(
                    response.data().transactionKey(),
                    response.data().orderId(),
                    PaymentStatus.valueOf(response.data().status().toUpperCase()),
                    response.data().reason()
            );

        } catch (Exception e) {
            return PaymentQueryResult.failed("조회 실패: " + e.getMessage());
        }
    }

    @Override
    public PaymentHistoryResult queryPaymentHistory(String userId, String orderId) {
        try {
            PgClientDto.PgClientHistoryResponse response = pgClient.getPaymentsByOrderId(userId, orderId);
            return PaymentHistoryResult.success(response);
        } catch (Exception e) {
            return PaymentHistoryResult.failed("결제 내역 조회 실패: " + e.getMessage());
        }
    }


}
