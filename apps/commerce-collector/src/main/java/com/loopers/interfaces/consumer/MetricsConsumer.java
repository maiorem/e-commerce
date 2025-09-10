package com.loopers.interfaces.consumer;

import com.loopers.application.event.MetricsApplicationService;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.OrderCreatedEvent;
import com.loopers.event.ProductViewedEvent;
import com.loopers.infrastructure.event.ConsumerEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsConsumer {

    private final MetricsApplicationService metricsService;
    private final ConsumerEventMapper eventMapper;

    /**
     * 상품 조회 이벤트 배치 처리 - 조회수 메트릭 업데이트
     */
    @KafkaListener(
            topics = "${kafka.topics.view-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleProductViewedEventsBatch(
            @Payload List<Map<String, Object>> eventDataList,
            Acknowledgment ack
    ) {
        log.debug("상품 조회 이벤트 배치 처리 시작 - 메시지 수: {}", eventDataList.size());

        try {
            for (Map<String, Object> eventData : eventDataList) {
                ProductViewedEvent event = eventMapper.toProductViewedEvent(eventData);
                metricsService.handleProductViewedEvent(event);
            }
            
            ack.acknowledge();
            log.debug("상품 조회 메트릭 배치 처리 완료 - 처리된 메시지 수: {}", eventDataList.size());

        } catch (Exception e) {
            log.error("상품 조회 메트릭 배치 처리 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 좋아요 변경 이벤트 배치 처리 - 좋아요 수 메트릭 업데이트
     */
    @KafkaListener(
            topics = "${kafka.topics.like-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeChangedEventsBatch(
            @Payload List<Map<String, Object>> eventDataList,
            Acknowledgment ack
    ) {
        log.debug("좋아요 변경 이벤트 배치 처리 시작 - 메시지 수: {}", eventDataList.size());

        try {
            for (Map<String, Object> eventData : eventDataList) {
                LikeChangedEvent event = eventMapper.toLikeChangedEvent(eventData);
                metricsService.handleLikeChangedEvent(event);
            }
            
            ack.acknowledge();
            log.debug("좋아요 변경 메트릭 배치 처리 완료 - 처리된 메시지 수: {}", eventDataList.size());

        } catch (Exception e) {
            log.error("좋아요 변경 메트릭 배치 처리 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 주문 생성 이벤트 배치 처리 - 주문 메트릭 업데이트
     */
    @KafkaListener(
            topics = "${kafka.topics.order-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleOrderCreatedEventsBatch(
            @Payload List<Map<String, Object>> eventDataList,
            Acknowledgment ack
    ) {
        log.debug("주문 생성 이벤트 배치 처리 시작 - 메시지 수: {}", eventDataList.size());
        
        try {
            for (Map<String, Object> eventData : eventDataList) {
                OrderCreatedEvent event = eventMapper.toOrderCreatedEvent(eventData);
                metricsService.handleOrderCreatedEvent(event);
            }
            
            ack.acknowledge();
            log.debug("주문 생성 메트릭 배치 처리 완료 - 처리된 메시지 수: {}", eventDataList.size());

        } catch (Exception e) {
            log.error("주문 생성 메트릭 배치 처리 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}
