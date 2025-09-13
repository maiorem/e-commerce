package com.loopers.application.like;

import com.loopers.domain.like.event.LikeChangePublisher;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserId;
import com.loopers.domain.like.event.LikeChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.retry.annotation.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeCountProcessor {

    private final ProductRepository productRepository;
    private final LikeChangePublisher likeChangePublisher;


    @Retry(name = "optimisticLockRetry")
    @Transactional
    public void updateProductLikeCount(Long productId, UserId userId, int countDelta) {
        try {
            ProductModel product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

            int oldCount = product.getLikesCount();

            if (countDelta > 0) {
                product.incrementLikesCount();
                log.debug("[LikeEventHandler] 좋아요 수 증가 - ProductId: {}, 현재 수: {}",
                        productId, product.getLikesCount());
                int newCount = product.getLikesCount();

                likeChangePublisher.publish(LikeChangedEvent.liked(productId, userId.getValue(), oldCount, newCount));

            } else {
                product.decrementLikesCount();
                log.debug("[LikeEventHandler] 좋아요 수 감소 - ProductId: {}, 현재 수: {}",
                        productId, product.getLikesCount());

                int newCount = product.getLikesCount();

                likeChangePublisher.publish(LikeChangedEvent.unliked(productId, userId.getValue(), oldCount, newCount));
            }

            productRepository.save(product);

        } catch (Exception e) {
            log.error("[LikeEventHandler] 좋아요 수 업데이트 실패 - ProductId: {}, CountDelta: {}, Error: {}",
                    productId, countDelta, e.getMessage(), e);
        }
    }
}
