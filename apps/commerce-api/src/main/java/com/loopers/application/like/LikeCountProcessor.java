package com.loopers.application.like;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
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


    @Retry(name = "optimisticLockRetry")
    @Transactional
    public void updateProductLikeCount(Long productId, int countDelta) {
        try {
            ProductModel product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

            if (countDelta > 0) {
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
            log.error("[LikeEventHandler] 좋아요 수 업데이트 실패 - ProductId: {}, CountDelta: {}, Error: {}",
                    productId, countDelta, e.getMessage(), e);
        }
    }
}
