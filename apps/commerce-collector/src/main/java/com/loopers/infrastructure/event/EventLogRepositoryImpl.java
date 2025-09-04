package com.loopers.infrastructure.event;

import com.loopers.domain.entity.EventLog;
import com.loopers.domain.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepository {

    private final EventLogJpaRepository jpaRepository;

    @Override
    public EventLog save(EventLog eventLog) {
        return jpaRepository.save(eventLog);
    }

    @Override
    public List<EventLog> saveAll(List<EventLog> eventLogs) {
        return jpaRepository.saveAll(eventLogs);
    }

    @Override
    public Optional<EventLog> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId);
    }
}
