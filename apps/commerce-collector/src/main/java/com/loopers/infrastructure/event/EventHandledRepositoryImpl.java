package com.loopers.infrastructure.event;

import com.loopers.domain.entity.EventHandled;
import com.loopers.domain.repository.EventHandledRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventHandledRepositoryImpl implements EventHandledRepository {

    private final EventHandledJpaRepository jpaRepository;

    @Override
    public boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup) {
        return jpaRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup);
    }

    @Override
    public EventHandled save(EventHandled eventHandled) {
        return jpaRepository.save(eventHandled);
    }

    @Override
    public List<EventHandled> saveAll(List<EventHandled> eventHandleds) {
        return jpaRepository.saveAll(eventHandleds);
    }

    @Override
    public Set<String> findProcessedEventIds(Set<String> eventIds, String consumerGroup) {
        return jpaRepository.findEventIdsByEventIdInAndConsumerGroup(eventIds, consumerGroup)
                .stream()
                .map(EventHandled::getEventId)
                .collect(Collectors.toSet());
    }
}
