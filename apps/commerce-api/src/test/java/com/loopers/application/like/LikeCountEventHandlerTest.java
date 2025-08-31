package com.loopers.application.like;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LikeCountEventHandlerTest {

    @InjectMocks
    private LikeCountEventHandler likeCountEventHandler;

    private UserId userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        userId = UserId.of("testuser");
        productId = 1L;
    }

    @Nested
    @DisplayName("상품 좋아요 이벤트 처리 시")
    class Handle_Product_Liked_Event {

        @Test
        @DisplayName("상품 좋아요 이벤트가 정상적으로 처리된다")
        void handleProductLikedEvent() {
            ProductLikedEvent event = ProductLikedEvent.create(productId, userId);

            likeCountEventHandler.handleProductLiked(event);
        }
    }

    @Nested
    @DisplayName("상품 좋아요 취소 이벤트 처리 시")
    class Handle_Product_Unliked_Event {

        @Test
        @DisplayName("상품 좋아요 취소 이벤트가 정상적으로 처리된다")
        void handleProductUnlikedEvent() {
            ProductUnlikedEvent event = ProductUnlikedEvent.create(productId, userId);

            likeCountEventHandler.handleProductUnliked(event);
        }
    }

    @Nested
    @DisplayName("이벤트 데이터 검증")
    class Event_Data_Validation {

        @Test
        @DisplayName("ProductLikedEvent의 데이터가 올바르게 생성된다")
        void productLikedEventCreation() {
            ProductLikedEvent event = ProductLikedEvent.create(productId, userId);

            assertThat(event.getProductId()).isEqualTo(productId);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getOccurredAt()).isNotNull();
        }

        @Test
        @DisplayName("ProductUnlikedEvent의 데이터가 올바르게 생성된다")
        void productUnlikedEventCreation() {
            ProductUnlikedEvent event = ProductUnlikedEvent.create(productId, userId);

            assertThat(event.getProductId()).isEqualTo(productId);
            assertThat(event.getUserId()).isEqualTo(userId);
            assertThat(event.getOccurredAt()).isNotNull();
        }
    }
}
