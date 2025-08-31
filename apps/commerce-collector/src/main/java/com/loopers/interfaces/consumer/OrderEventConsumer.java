package com.loopers.interfaces.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OrderEventConsumer {

    @KafkaListener(
            topics = "${kafka.topics.order-events:order-events}",
            containerFactory = "BATCH_LISTENER_DEFAULT",
            groupId = "${kafka.consumer.group-id:loopers-default-consumer}"
    )
    public void handleOrderEvents(List<ConsumerRecord<String, Object>> messages, Acknowledgment acknowledgment) {

        try {
            log.info("Processing {} order events", messages.size());

            for (ConsumerRecord<String, Object> message : messages) {
                processEvent(message);
            }
            acknowledgment.acknowledge();
            log.info("Successfully processed {} order events", messages.size());

        } catch (Exception e) {
            log.error("Failed to process order events", e);



        }
    }

    private void processEvent(ConsumerRecord<String, Object> message) {
        String key = message.key(); // orderId
        Object payload = message.value(); // event data

        log.info("Processing event: key={}, payload={}", key, payload);

    }

}
