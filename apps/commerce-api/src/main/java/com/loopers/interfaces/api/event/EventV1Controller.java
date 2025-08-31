package com.loopers.interfaces.api.event;

import com.loopers.domain.product.event.ProductClickedEvent;
import com.loopers.domain.product.event.ProductClickedPublisher;
import com.loopers.domain.user.UserId;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventV1Controller implements EventV1ApiSpec {

    private final ProductClickedPublisher eventPublisher;

    @PostMapping("/click")
    public ApiResponse<?> handleProductClick(String userId, EventV1Dto.ProductClickRequest request) {
        if (userId != null) {
            eventPublisher.publish(ProductClickedEvent.create(request.productId(), UserId.of(userId), request.context()));
        }
        return ApiResponse.success();
    }

}
