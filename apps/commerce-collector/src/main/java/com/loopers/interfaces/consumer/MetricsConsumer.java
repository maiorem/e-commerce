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

import java.util.Map;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsConsumer {

    private final MetricsApplicationService metricsService;
    private final ConsumerEventMapper eventMapper;

    /**
     * 상품 조회 이벤트 처리 - 조회수 메트릭 업데이트
     */
    @KafkaListener(
            topics = "${kafka.topics.view-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleProductViewedEvent(
            @Payload Map<String, Object> eventData,
            Acknowledgment ack
    ) {
        ProductViewedEvent event = eventMapper.toProductViewedEvent(eventData);
        log.debug("상품 조회 이벤트 수신 - EventId: {}, ProductId: {}",
                event.getEventId(), event.getProductId());

        try {
            metricsService.handleProductViewedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("상품 조회 메트릭 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
            throw e; // DLQ로 전송하기 위해 예외 재발생
        }
    }

    /**
     * 좋아요 변경 이벤트 처리 - 좋아요 수 메트릭 업데이트
     */
    @KafkaListener(
            topics = "${kafka.topics.like-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleLikeChangedEvent(
            @Payload Map<String, Object> eventData,
            Acknowledgment ack
    ) {
        LikeChangedEvent event = eventMapper.toLikeChangedEvent(eventData);
        log.debug("좋아요 변경 이벤트 수신 - EventId: {}, ProductId: {}",
                event.getEventId(), event.getProductId());

        try {
            metricsService.handleLikeChangedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("좋아요 메트릭 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
            throw e; // DLQ로 전송하기 위해 예외 재발생
        }
    }

    /**
     * 주문 생성 이벤트 처리 -
     */
    @KafkaListener(
            topics = "${kafka.topics.order-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleOrderCreatedEvent(
            @Payload Map<String, Object> eventData,
            Acknowledgment ack
    ) {
        OrderCreatedEvent event = eventMapper.toOrderCreatedEvent(eventData);
        log.debug("주문 생성 이벤트 수신 - EventId: {}, OrderId: {}",
                event.getEventId(), event.getOrderId());
        try {
            metricsService.handleOrderCreatedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("주문 메트릭 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
            throw e; // DLQ로 전송하기 위해 예외 재발생
        }
    }
}
