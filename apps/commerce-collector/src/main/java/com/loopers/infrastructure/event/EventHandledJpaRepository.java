package com.loopers.infrastructure.event;

import com.loopers.domain.entity.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {
    boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup);

    List<EventHandled> findEventIdsByEventIdInAndConsumerGroup(Set<String> eventIds, String consumerGroup);
}
