package com.loopers.infrastructure.pg_client;

import com.loopers.support.http.config.FeignClientTimeoutConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "pgClient",
        url = "http://localhost:8082",
        configuration = FeignClientTimeoutConfig.class
)
public interface PgClient {

    @PostMapping("/api/v1/payments")
    PgClientDto.PgClientResponse requestPayment(@RequestBody PgClientDto.PgClientRequest request);

}
