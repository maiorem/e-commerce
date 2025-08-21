package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface OrderV1ApiSpec {

    ApiResponse<OrderV1Dto.CreateOrderResponse> createOrder(@RequestHeader("X-USER_ID") String userId, @RequestBody OrderV1Dto.CreateOrderRequest request);

}
