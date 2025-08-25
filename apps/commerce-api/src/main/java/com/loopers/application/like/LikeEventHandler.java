package com.loopers.application.like;


import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
import com.loopers.domain.user.event.UserActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventHandler {

    private final ProductRepository productRepository;
    private final UserActionTrackingPort userActionTrackingPort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional
    public void handleProductLiked(ProductLikedEvent event) {
        log.info("[LikeEventHandler] 좋아요 이벤트 처리 시작 - ProductId: {}, UserId: {}",
                event.getProductId(), event.getUserId().getValue());

        try {
            // 1. 상품 좋아요 수 증가 (eventual consistency)
            updateProductLikeCount(event.getProductId(), 1);

            // 2. 사용자 행동 로깅
            trackUserLikeAction(event);

            log.info("[LikeEventHandler] 좋아요 후속 처리 완료 - ProductId: {}", event.getProductId());

        } catch (Exception e) {
            log.error("[LikeEventHandler] 좋아요 이벤트 처리 중 예외 발생 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);

        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Transactional
    public void handleProductUnliked(ProductUnlikedEvent event) {
        log.info("[LikeEventHandler] 좋아요 취소 이벤트 처리 시작 - ProductId: {}, UserId: {}",
                event.getProductId(), event.getUserId().getValue());

        try {
            // 1. 상품 좋아요 수 감소 (eventual consistency)
            updateProductLikeCount(event.getProductId(), -1);

            // 2. 사용자 행동 로깅
            trackUserUnlikeAction(event);

            log.info("[LikeEventHandler] 좋아요 취소 후속 처리 완료 - ProductId: {}", event.getProductId());

        } catch (Exception e) {
            log.error("[LikeEventHandler] 좋아요 취소 이벤트 처리 중 예외 발생 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }
    }

    private void updateProductLikeCount(Long productId, int delta) {
        try {
            ProductModel product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

            if (delta > 0) {
                product.incrementLikesCount();
                log.debug("[LikeEventHandler] 좋아요 수 증가 - ProductId: {}, 현재 수: {}",
                        productId, product.getLikesCount());
            } else {
                product.decrementLikesCount();
                log.debug("[LikeEventHandler] 좋아요 수 감소 - ProductId: {}, 현재 수: {}",
                        productId, product.getLikesCount());
            }

            productRepository.save(product);

        } catch (Exception e) {
            log.error("[LikeEventHandler] 좋아요 수 업데이트 실패 - ProductId: {}, Delta: {}, Error: {}",
                    productId, delta, e.getMessage(), e);
        }
    }

    private void trackUserLikeAction(ProductLikedEvent event) {
        try {
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.PRODUCT_LIKE,
                    event.getProductId()
            );

            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("[LikeEventHandler] 사용자 행동 로깅 실패 - ProductId: {}, UserId: {}, Error: {}",
                    event.getProductId(), event.getUserId().getValue(), e.getMessage(), e);
        }
    }

    private void trackUserUnlikeAction(ProductUnlikedEvent event) {
        try {
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.PRODUCT_UNLIKE,
                    event.getProductId()
            );

            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("[LikeEventHandler] 사용자 행동 로깅 실패 - ProductId: {}, UserId: {}, Error: {}",
                    event.getProductId(), event.getUserId().getValue(), e.getMessage(), e);
        }
    }
}
