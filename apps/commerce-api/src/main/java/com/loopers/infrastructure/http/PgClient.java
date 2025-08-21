package com.loopers.infrastructure.http;

import com.loopers.support.http.config.FeignClientTimeoutConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "pgClient",
        url = "http://localhost:8082",
        configuration = FeignClientTimeoutConfig.class
)
public interface PgClient {

    @PostMapping("/api/v1/payments")
    PgClientDto.PgClientResponse requestPayment(@RequestHeader("X-USER-ID") String userId, @RequestBody PgClientDto.PgClientRequest request);

    @GetMapping("/api/v1/payments/{transactionKey}")
    PgClientDto.PgClientQueryResponse getTransaction(@RequestHeader("X-USER-ID") String userId, @PathVariable("transactionKey") String transactionKey);

    @GetMapping("/api/v1/payments")
    PgClientDto.PgClientHistoryResponse getPaymentsByOrderId(@RequestHeader("X-USER-ID") String userId, @RequestParam("orderId") String orderId);

}
