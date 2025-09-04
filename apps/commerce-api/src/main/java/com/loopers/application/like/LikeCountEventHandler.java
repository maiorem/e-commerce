package com.loopers.application.like;


import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeCountEventHandler {

    private final LikeCountProcessor likeCountProcessor;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductLiked(ProductLikedEvent event) {
        log.info("[LikeEventHandler] 좋아요 이벤트 처리 시작 - ProductId: {}, UserId: {}",
                event.getProductId(), event.getUserId().getValue());

        try {
            // 상품 좋아요 수 증가
            likeCountProcessor.updateProductLikeCount(event.getProductId(), event.getUserId(), 1);

            log.info("[LikeEventHandler] 좋아요 후속 처리 완료 - ProductId: {}", event.getProductId());

        } catch (Exception e) {
            log.error("[LikeEventHandler] 좋아요 이벤트 처리 중 예외 발생 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);

        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUnliked(ProductUnlikedEvent event) {
        log.info("[LikeEventHandler] 좋아요 취소 이벤트 처리 시작 - ProductId: {}, UserId: {}",
                event.getProductId(), event.getUserId().getValue());

        try {
            // 1. 상품 좋아요 수 감소
            likeCountProcessor.updateProductLikeCount(event.getProductId(), event.getUserId(), -1);

            log.info("[LikeEventHandler] 좋아요 취소 후속 처리 완료 - ProductId: {}", event.getProductId());

        } catch (Exception e) {
            log.error("[LikeEventHandler] 좋아요 취소 이벤트 처리 중 예외 발생 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }
    }


}
