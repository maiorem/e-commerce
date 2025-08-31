package com.loopers.interfaces.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CatalogEventConsumer {

    @KafkaListener(
            topics = "${kafka.topics.catalog-events:catalog-events}",
            containerFactory = "BATCH_LISTENER_DEFAULT",
            groupId = "${kafka.consumer.group-id:loopers-default-consumer}"
    )
    public void handleCatalogEvents(List<ConsumerRecord<String, Object>> messages, Acknowledgment acknowledgment) {

        try {
            log.info("Processing {} catalog events", messages.size());

            for (ConsumerRecord<String, Object> message : messages) {
                processEvent(message);
            }
            acknowledgment.acknowledge();
            log.info("Successfully processed {} catalog events", messages.size());

        } catch (Exception e) {
            log.error("Failed to process catalog events", e);



        }
    }

    private void processEvent(ConsumerRecord<String, Object> message) {
        String key = message.key(); // productId
        Object payload = message.value(); // event data

        log.info("Processing event: key={}, payload={}", key, payload);

    }
}
