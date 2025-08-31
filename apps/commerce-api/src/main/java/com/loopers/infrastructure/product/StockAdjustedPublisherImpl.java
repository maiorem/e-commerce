package com.loopers.infrastructure.product;

import com.loopers.domain.product.event.StockAdjustedEvent;
import com.loopers.domain.product.event.StockAdjustedPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAdjustedPublisherImpl implements StockAdjustedPublisher {

    @Value("${kafka.topics.catalog-events}")
    private String catalogTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publish(StockAdjustedEvent event) {
        try {
            String key = event.getProductId().toString();

            log.info("Kafka 재고 변경 이벤트 발행 - ProductId: {}, OldStock: {}, NewStock: {}",
                    event.getProductId(), event.getOldStock(), event.getNewStock());

            kafkaTemplate.send(catalogTopic, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("재고 변경 이벤트 발행 실패 - ProductId: {}, Error: {}",
                                    event.getProductId(), ex.getMessage(), ex);
                        } else {
                            log.info("재고 변경 이벤트 발행 성공 - ProductId: {}, Offset: {}",
                                    event.getProductId(), result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception e) {
            log.error("재고 변경 이벤트 발행 중 예외 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }

    }
}
