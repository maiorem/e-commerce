package com.loopers.application.event;


import com.loopers.domain.entity.EventHandled;
import com.loopers.domain.entity.EventLog;
import com.loopers.domain.repository.EventHandledRepository;
import com.loopers.domain.repository.EventLogRepository;
import com.loopers.event.LikeChangedEvent;
import com.loopers.event.ProductViewedEvent;
import com.loopers.event.StockAdjustedEvent;
import com.loopers.supoort.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogApplicationService {

    private final EventHandledRepository eventHandledRepository;
    private final EventLogRepository eventLogRepository;

    private static final String CONSUMER_GROUP = "audit-log-group";

    public void handleEventsBatch(List<ConsumerRecord<String, Object>> records) {
        log.info("감사 로그 배치 처리 시작 - 메시지 수: {}", records.size());

        // 이미 처리된 이벤트 ID들 조회
        Set<String> eventIds = records.stream()
                .map(record -> extractEventId(record.value()))
                .collect(Collectors.toSet());

        Set<String> processedEventIds = eventHandledRepository.findProcessedEventIds(eventIds, CONSUMER_GROUP);

        // 새로운 이벤트들만 처리
        List<ConsumerRecord<String, Object>> newRecords = records.stream()
                .filter(record -> !processedEventIds.contains(extractEventId(record.value())))
                .toList();

        if (newRecords.isEmpty()) {
            log.info("처리할 새로운 이벤트가 없음");
            return;
        }

        // EventLog 생성
        List<EventLog> eventLogs = newRecords.stream()
                .map(this::convertToEventLog)
                .toList();

        // 배치 저장
        eventLogRepository.saveAll(eventLogs);

        // 처리 완료 기록
        List<EventHandled> eventHandleds = eventLogs.stream()
                .map(log -> EventHandled.of(log.getEventId(), CONSUMER_GROUP))
                .toList();
        eventHandledRepository.saveAll(eventHandleds);

        log.info("감사 로그 배치 처리 완료 - 저장된 로그 수: {}", eventLogs.size());
    }

    private EventLog convertToEventLog(ConsumerRecord<String, Object> record) {
        Object event = record.value();
        String eventId = extractEventId(event);

        return EventLog.builder()
                .eventId(eventId)
                .eventType(event.getClass().getSimpleName())
                .topic(record.topic())
                .partition(record.partition())
                .offset(record.offset())
                .payload(JsonUtils.toJson(event))
                .occurredAt(ZonedDateTime.now())
                .build();
    }

    private String extractEventId(Object event) {
        if (event instanceof StockAdjustedEvent stockEvent) {
            return stockEvent.getEventId();
        } else if (event instanceof LikeChangedEvent likeEvent) {
            return likeEvent.getEventId();
        } else if (event instanceof ProductViewedEvent viewEvent) {
            return viewEvent.getEventId();
        }
        // 기타 이벤트 타입들...
        return "unknown-" + System.currentTimeMillis();
    }

}
