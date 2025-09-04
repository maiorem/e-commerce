package com.loopers.domain.repository;

import com.loopers.domain.entity.EventHandled;

import java.util.List;
import java.util.Set;

public interface EventHandledRepository {

    boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup);
    EventHandled save(EventHandled eventHandled);
    List<EventHandled> saveAll(List<EventHandled> eventHandleds);
    Set<String> findProcessedEventIds(Set<String> eventIds, String consumerGroup);
}
