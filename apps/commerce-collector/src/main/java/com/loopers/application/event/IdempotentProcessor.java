package com.loopers.application.event;

import com.loopers.domain.entity.EventHandled;
import com.loopers.domain.repository.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IdempotentProcessor {

    private final EventHandledRepository eventHandledRepository;

    public boolean isAlreadyProcessed(String eventId, String consumerGroup) {
        return eventHandledRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup);
    }

    public void markAsProcessed(String eventId, String consumerGroup) {
        eventHandledRepository.save(EventHandled.of(eventId, consumerGroup));
    }
}
