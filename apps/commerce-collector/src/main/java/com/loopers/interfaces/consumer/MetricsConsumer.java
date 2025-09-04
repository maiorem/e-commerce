package com.loopers.interfaces.consumer;

import com.loopers.application.event.MetricsApplicationService;
import com.loopers.config.kafka.KafkaConfig;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.OrderCreatedEvent;
import com.loopers.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MetricsConsumer {

    private final MetricsApplicationService metricsService;

    /**
     * 상품 조회 이벤트 처리 - 조회수 메트릭 업데이트
     */
    @KafkaListener(
            topics = "${kafka.topics.view-events}",
            groupId = "metrics-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleProductViewedEvent(
            @Payload ProductViewedEvent event,
            Acknowledgment ack
    ) {
        log.debug("상품 조회 이벤트 수신 - EventId: {}, ProductId: {}",
                event.getEventId(), event.getProductId());

        try {
            metricsService.handleProductViewedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("상품 조회 메트릭 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
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
            @Payload LikeChangedEvent event,
            Acknowledgment ack
    ) {
        log.debug("좋아요 변경 이벤트 수신 - EventId: {}, ProductId: {}",
                event.getEventId(), event.getProductId());

        try {
            metricsService.handleLikeChangedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("좋아요 메트릭 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
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
            @Payload OrderCreatedEvent event,
            Acknowledgment ack
    ) {
        log.debug("주문 생성 이벤트 수신 - EventId: {}, OrderId: {}",
                event.getEventId(), event.getOrderId());
        try {
            metricsService.handleOrderCreatedEvent(event);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("주문 메트릭 처리 실패 - EventId: {}, Error: {}",
                    event.getEventId(), e.getMessage(), e);
        }
    }
}
