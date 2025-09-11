package com.loopers.application.event;


import com.loopers.domain.repository.ProductMetricsRepository;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.OrderCreatedEvent;
import com.loopers.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsApplicationService {

    private final IdempotentProcessor idempotentProcessor;
    private final ProductMetricsRepository productMetricsRepository;

    private static final String CONSUMER_GROUP = "metrics-group";

    public void handleProductViewedEvent(ProductViewedEvent event) {
        String eventId = event.getEventId();

        if (idempotentProcessor.isAlreadyProcessed(eventId, CONSUMER_GROUP)) {
            log.debug("이미 처리된 상품 조회 이벤트 - EventId: {}", eventId);
            return;
        }

        // 조회수 증가
        productMetricsRepository.upsertViewCount(
                event.getProductId(),
                event.getOccurredAt()
        );

        idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);

        log.debug("상품 조회 메트릭 업데이트 완료 - ProductId: {}", event.getProductId());
    }

    public void handleLikeChangedEvent(LikeChangedEvent event) {
        String eventId = event.getEventId();

        if (idempotentProcessor.isAlreadyProcessed(eventId, CONSUMER_GROUP)) {
            log.debug("이미 처리된 좋아요 변경 이벤트 - EventId: {}", eventId);
            return;
        }

        // 좋아요 수 업데이트
        productMetricsRepository.upsertLikeCount(
                event.getProductId(),
                (long) event.getNewLikeCount(),
                event.getOccurredAt()
        );

        idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);

        log.debug("좋아요 메트릭 업데이트 완료 - ProductId: {}, LikeCount: {}",
                event.getProductId(), event.getNewLikeCount());
    }

    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        String eventId = event.getEventId();

        if (idempotentProcessor.isAlreadyProcessed(eventId, CONSUMER_GROUP)) {
            log.debug("이미 처리된 주문 생성 이벤트 - EventId: {}", eventId);
            return;
        }
        
        // 주문 아이템별 판매 수와 금액 업데이트
        event.getOrderItems().forEach(item -> {
            long totalAmount = (long) item.getPrice() * item.getQuantity();
            productMetricsRepository.upsertSalesCount(item.getProductId(), totalAmount);
            
            log.debug("주문 상품 메트릭 업데이트 - ProductId: {}, Amount: {}", 
                     item.getProductId(), totalAmount);
        });
        
        idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);

        log.info("주문 메트릭 업데이트 완료 - OrderId: {}, 상품 수: {}", 
                 event.getOrderId(), event.getOrderItems().size());
    }


}
