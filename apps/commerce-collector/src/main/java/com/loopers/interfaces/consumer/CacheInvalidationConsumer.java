package com.loopers.interfaces.consumer;

import com.loopers.application.event.CacheInvalidationApplicationService;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.StockAdjustedEvent;
import com.loopers.application.event.ConsumerEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationConsumer {

    private final CacheInvalidationApplicationService cacheInvalidationService;
    private final ConsumerEventMapper eventMapper;

    /**
     * 재고 조정 이벤트 처리 - 재고 소진 시 캐시 무효화
     */
    @KafkaListener(
            topics = "${kafka.topics.stock-events}",
            groupId = "cache-invalidation-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleStockAdjustedEvent(
            @Payload Map<String, Object> eventData,
            @Header Map<String, Object> headers,
            Acknowledgment ack
    ) {
        StockAdjustedEvent event = eventMapper.toStockAdjustedEvent(eventData);
        Long offset = (Long) headers.get(KafkaHeaders.OFFSET);
        Integer partition = (Integer) headers.get(KafkaHeaders.RECEIVED_PARTITION);

        log.info("재고 조정 이벤트 수신 - EventId: {}, ProductId: {}, Partition: {}, Offset: {}",
                event.getEventId(), event.getProductId(), partition, offset);

        try {
            cacheInvalidationService.handleStockAdjustedEvent(event);
            ack.acknowledge();

            log.debug("재고 조정 이벤트 처리 및 오프셋 커밋 완료 - EventId: {}", event.getEventId());

        } catch (Exception e) {
            log.error("재고 조정 이벤트 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
            throw e; // DLQ로 전송하기 위해 예외 재발생
        }
    }

    /**
     * 좋아요 변경 이벤트 처리 - 좋아요 수 변경 시 캐시 무효화
     */
    @KafkaListener(
            topics = "${kafka.topics.like-events}",
            groupId = "cache-invalidation-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeChangedEvent(
            @Payload Map<String, Object> eventData,
            Acknowledgment ack
    ) {
        LikeChangedEvent event = eventMapper.toLikeChangedEvent(eventData);
        log.info("좋아요 변경 이벤트 수신 - EventId: {}, ProductId: {}",
                event.getEventId(), event.getProductId());

        try {
            cacheInvalidationService.handleLikeChangedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("좋아요 변경 이벤트 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
            throw e; // DLQ로 전송하기 위해 예외 재발생
        }
    }

}
