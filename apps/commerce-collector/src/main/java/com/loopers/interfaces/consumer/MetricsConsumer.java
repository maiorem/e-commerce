package com.loopers.interfaces.consumer;

import com.loopers.application.event.MetricsApplicationService;
import com.loopers.application.event.MetricsBatchAggregator;
import com.loopers.application.event.ProductMetricsAggregation;
import com.loopers.config.kafka.KafkaConfig;
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
    private final MetricsBatchAggregator batchAggregator;

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
        handleEventsBatchOptimized(eventDataList, ack, "VIEW");
    }

    /**
     * 좋아요 변경 이벤트 배치 처리 - 좋아요 수 메트릭 업데이트 (최적화)
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
        handleEventsBatchOptimized(eventDataList, ack, "LIKE");
    }

    /**
     * 주문 생성 이벤트 배치 처리 - 주문 메트릭 업데이트 (최적화)
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
        handleEventsBatchOptimized(eventDataList, ack, "ORDER");
    }
    
    /**
     * 메모리 집계 후 배치 DB 업데이트
     */
    private void handleEventsBatchOptimized(List<Map<String, Object>> eventDataList, 
                                           Acknowledgment ack, 
                                           String eventType) {
        log.info("메트릭 배치 처리 시작 - EventType: {}, 메시지 수: {}", eventType, eventDataList.size());
        
        try {
            // 1. 메모리에서 집계
            ProductMetricsAggregation aggregation = batchAggregator.aggregateEvents(eventDataList);
            
            // 2. 집계된 데이터를 배치로 DB 업데이트
            metricsService.updateMetricsBatch(aggregation);
            
            // 3. 멱등성 처리
            metricsService.markEventsAsProcessed(eventDataList);
            
            ack.acknowledge();
            
            log.info("메트릭 배치 처리 완료 - EventType: {}, 처리된 상품 수: View={}, Like={}, Sales={}", 
                    eventType,
                    aggregation.getViewCounts().size(),
                    aggregation.getLikeUpdates().size(), 
                    aggregation.getSalesData().size());
            
        } catch (Exception e) {
            log.error("메트릭 배치 처리 실패 - EventType: {}, Error: {}", eventType, e.getMessage(), e);
            throw e;
        }
    }
}
