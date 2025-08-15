package com.loopers.application.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCacheEventListener {

    private final ProductCacheService productCacheService;

    /**
     * 상품 생성/수정 시 캐시 무효화
     */
    @EventListener
    public void handleProductChanged(ProductChangedEvent event) {
        log.info("상품 변경 이벤트 감지 - 상품ID: {}, 변경 타입: {}", 
                event.getProductId(), event.getChangeType());
        
        switch (event.getChangeType()) {
            case CREATED:
            case UPDATED:
                // 상품 상세 캐시 무효화
                productCacheService.evictProductDetailCache(event.getProductId());
                // 첫 페이지 캐시 무효화 (상품 순서 변경 가능성)
                productCacheService.evictFirstPageCache();
                break;
            case DELETED:
                // 상품 상세 캐시 무효화
                productCacheService.evictProductDetailCache(event.getProductId());
                // 첫 페이지 캐시 무효화
                productCacheService.evictFirstPageCache();
                break;
        }
    }

    /**
     * 상품 변경 이벤트
     */
    public static class ProductChangedEvent {
        private final Long productId;
        private final ChangeType changeType;

        public ProductChangedEvent(Long productId, ChangeType changeType) {
            this.productId = productId;
            this.changeType = changeType;
        }

        public Long getProductId() {
            return productId;
        }

        public ChangeType getChangeType() {
            return changeType;
        }
    }

    /**
     * 변경 타입
     */
    public enum ChangeType {
        CREATED, UPDATED, DELETED
    }
} 
