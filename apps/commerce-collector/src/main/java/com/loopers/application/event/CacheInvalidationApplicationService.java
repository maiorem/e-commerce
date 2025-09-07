package com.loopers.application.event;

import com.loopers.domain.entity.ConsumerLastProcessed;
import com.loopers.domain.repository.ConsumerLastProcessedRepository;
import com.loopers.domain.service.CacheInvalidationDomainService;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.StockAdjustedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationApplicationService {

    private final IdempotentProcessor idempotentProcessor;
    private final CacheInvalidationDomainService cacheInvalidationDomainService;
    private final ConsumerLastProcessedRepository lastProcessedRepository;

    private static final String CONSUMER_GROUP = "cache-invalidation-group";

    @Transactional
    public void handleStockAdjustedEvent(StockAdjustedEvent event) {
        String eventId = event.getEventId();
        String aggregateId = "product-" + event.getProductId();

        // 중복 이벤트 체크
        if (idempotentProcessor.isAlreadyProcessed(eventId, CONSUMER_GROUP)) {
            log.info("이미 처리된 재고 조정 이벤트 - EventId: {}", eventId);
            return;
        }

        // 순서 체크
        ConsumerLastProcessed lastProcessed = lastProcessedRepository
                .findByConsumerGroupAndAggregateId(CONSUMER_GROUP, aggregateId)
                .orElse(null);

        if (lastProcessed != null && !lastProcessed.shouldProcess(event.getOccurredAt())) {
            log.warn("오래된 재고 이벤트 무시 - Event: {}, LastProcessed: {}",
                    event.getOccurredAt(), lastProcessed.getLastProcessedAt());

            // 오래되어 무시한 이벤트 완료 처리
            idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);
            return;
        }

        // 캐시 무효화
        if (cacheInvalidationDomainService.isStockDepletionCacheInvalidationNeeded(
                event.getOldStock(), event.getNewStock())) {

            cacheInvalidationDomainService.invalidateProductCache(
                    event.getProductId(), "stock_depleted");
            cacheInvalidationDomainService.invalidateProductListCache(
                    "latest", "stock_depleted");
        }

        // 마지막 처리 시간 업데이트
        if (lastProcessed == null) {
            lastProcessed = ConsumerLastProcessed.of(CONSUMER_GROUP, aggregateId, event.getOccurredAt());
        } else {
            lastProcessed.updateLastProcessedAt(event.getOccurredAt());
        }
        lastProcessedRepository.save(lastProcessed);

        // 처리 완료
        idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);

        log.info("재고 조정 이벤트 처리 완료 - {}", event);
    }


    @Transactional
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
