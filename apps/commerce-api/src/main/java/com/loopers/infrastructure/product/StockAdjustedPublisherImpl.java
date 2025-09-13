package com.loopers.infrastructure.product;

import com.loopers.domain.product.event.StockAdjustedPublisher;
import com.loopers.domain.product.event.StockAdjustedEvent;
import com.loopers.infrastructure.event.EventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAdjustedPublisherImpl implements StockAdjustedPublisher {

    @Value("${kafka.topics.stock-events}")
    private String stockTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventMapper eventMapper;

    @Override
    public void publish(StockAdjustedEvent event) {
        kafkaTemplate.send(stockTopic, event.getProductId().toString(), eventMapper.toConsumerEvent(event));
    }
}
