package com.loopers.application.event;

import com.loopers.domain.service.CacheInvalidationDomainService;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.StockAdjustedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationApplicationService {

    private final IdempotentProcessor idempotentProcessor;
    private final CacheInvalidationDomainService cacheInvalidationDomainService;

    private static final String CONSUMER_GROUP = "cache-invalidation-group";

    public void handleStockAdjustedEvent(StockAdjustedEvent event) {
        String eventId = event.getEventId();

        if (idempotentProcessor.isAlreadyProcessed(eventId, CONSUMER_GROUP)) {
            log.info("이미 처리된 재고 조정 이벤트 - EventId: {}", eventId);
            return;
        }

        // 재고 소진 시에만 캐시 무효화
        if (cacheInvalidationDomainService.isStockDepletionCacheInvalidationNeeded(
                event.getOldStock(), event.getNewStock())) {

            cacheInvalidationDomainService.invalidateProductCache(
                    event.getProductId(), "stock_depleted");
            cacheInvalidationDomainService.invalidateProductListCache(
                    "latest", "stock_depleted");
        }

        // 처리 완료 기록
        idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);

        log.info("재고 조정 이벤트 처리 완료 - ProductId: {}, OldStock: {}, NewStock: {}",
                event.getProductId(), event.getOldStock(), event.getNewStock());
    }

    public void handleLikeChangedEvent(LikeChangedEvent event) {
        String eventId = event.getEventId();

        if (idempotentProcessor.isAlreadyProcessed(eventId, CONSUMER_GROUP)) {
            log.info("이미 처리된 좋아요 변경 이벤트 - EventId: {}", eventId);
            return;
        }

        // 좋아요 수 변경 시 캐시 무효화
        if (cacheInvalidationDomainService.isLikeCacheInvalidationNeeded(
                event.getOldLikeCount(), event.getNewLikeCount())) {

            cacheInvalidationDomainService.invalidateProductCache(
                    event.getProductId(), "like_changed");
            cacheInvalidationDomainService.invalidateProductListCache(
                    "likes", "like_changed");
        }

        idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);

        log.info("좋아요 변경 이벤트 처리 완료 - ProductId: {}, OldCount: {}, NewCount: {}",
                event.getProductId(), event.getOldLikeCount(), event.getNewLikeCount());
    }

}
