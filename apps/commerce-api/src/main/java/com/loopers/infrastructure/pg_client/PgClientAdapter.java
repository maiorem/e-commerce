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
        return PaymentResult.success(response.data().transactionKey());
    }

    public PaymentResult fallback(PaymentData paymentData, Throwable t) {
        return PaymentResult.failed("결제에 실패하였습니다 : " + t.getMessage());
    }

    @Retry(name = "pgQueryRetry", fallbackMethod = "queryFallback")
    @Override
    public PaymentQueryResult queryPaymentStatus(String transactionKey) {
        try {
            PgClientDto.PgClientQueryResponse response = pgClient.getTransaction(transactionKey);

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

    public PaymentQueryResult queryFallback(String transactionKey, Throwable t) {
        return PaymentQueryResult.failed("결제 상태 조회 실패: " + t.getMessage());
    }


    @Override
    public PaymentHistoryResult queryPaymentHistory(String orderId) {
        try {
            PgClientDto.PgClientHistoryResponse response = pgClient.getPaymentsByOrderId(orderId);
            return PaymentHistoryResult.success(response);
        } catch (Exception e) {
            return PaymentHistoryResult.failed("결제 내역 조회 실패: " + e.getMessage());
        }
    }

}
