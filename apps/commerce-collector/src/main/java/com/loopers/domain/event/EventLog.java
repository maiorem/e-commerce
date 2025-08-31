package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLog extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "payload", columnDefinition = "JSON")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public EventLog(String eventId, String eventType, String aggregateType,
                    String aggregateId, String userId, String payload, LocalDateTime occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.userId = userId;
        this.payload = payload;
        this.occurredAt = occurredAt;
        this.processedAt = LocalDateTime.now();
    }

    public static EventLog create(String eventId, String eventType, String aggregateType,
                                  String aggregateId, String userId, String payload, LocalDateTime occurredAt) {
        return new EventLog(eventId, eventType, aggregateType, aggregateId, userId, payload, occurredAt);
    }
}
