package com.loopers.infrastructure.http;

import com.loopers.domain.payment.*;
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
    @CircuitBreaker(name = "pgCircuit", fallbackMethod = "fallback")
    @Retry(name = "pgRetry", fallbackMethod = "fallback")
    public PaymentResult processPayment(PaymentData paymentData) {
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
        } catch (Exception e) {
            log.error("PG 결제 요청 중 예외 발생 - OrderId: {}, Error: {}", 
                    paymentData.orderId(), e.getMessage(), e);
            return PaymentResult.failed("PG 결제 요청 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public PaymentResult fallback(PaymentData paymentData, Throwable t) {
        return PaymentResult.failed("결제에 실패하였습니다 : " + t.getMessage());
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
