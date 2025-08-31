package com.loopers.application.product;

import com.loopers.domain.product.event.ClickContext;
import com.loopers.domain.product.event.ProductClickedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserActingTrackingForProductEventHandlerTest {

    @InjectMocks
    private UserActingTrackingForProductEventHandler userActingTrackingForProductEventHandler;

    private Long productId;
    private UserId userId;

    @BeforeEach
    void setUp() {
        productId = 1L;
        userId = UserId.of("seyoung");
    }

    @Nested
    @DisplayName("상품 조회 이벤트 처리 시")
    class Handle_Product_Viewed_Event {

        @Test
        @DisplayName("상품 조회 이벤트가 정상적으로 처리된다")
        void handleProductViewedEvent() {
            ProductViewedEvent event = ProductViewedEvent.createDetailView(productId, userId);

            userActingTrackingForProductEventHandler.handleProductViewed(event);
        }
    }

    @Nested
    @DisplayName("상품 클릭 이벤트 처리 시")
    class Handle_Product_Clicked_Event {

        @Test
        @DisplayName("상품 클릭 이벤트가 정상적으로 처리된다")
        void handleProductClickedEvent() {
            ProductClickedEvent event = ProductClickedEvent.create(productId, userId, ClickContext.SEARCH_RESULT);

            userActingTrackingForProductEventHandler.handleProductClicked(event);
        }
    }

}
