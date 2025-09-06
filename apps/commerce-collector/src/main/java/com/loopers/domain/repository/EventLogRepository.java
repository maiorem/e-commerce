package com.loopers.domain.repository;

import com.loopers.domain.entity.EventLog;

import java.util.List;
import java.util.Optional;

public interface EventLogRepository {
    EventLog save(EventLog eventLog);
    List<EventLog> saveAll(List<EventLog> eventLogs);
    Optional<EventLog> findByEventId(String eventId);
}
