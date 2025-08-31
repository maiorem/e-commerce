package com.loopers.interfaces.api.event;

import com.loopers.interfaces.api.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public interface EventV1ApiSpec {

    ApiResponse<?> handleProductClick(@RequestHeader(value = "X-USER-ID", required = false) String userId, @RequestBody EventV1Dto.ProductClickRequest request);

}
