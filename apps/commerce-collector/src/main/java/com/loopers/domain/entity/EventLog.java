package com.loopers.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "event_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLog extends BaseEntity {

    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "partition_num")
    private Integer partition;

    @Column(name = "offset_num")
    private Long offset;

    @Column(name = "payload", columnDefinition = "JSON")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private ZonedDateTime occurredAt;

    @Builder
    public EventLog(String eventId, String eventType, String topic, Integer partition,
                    Long offset, String payload, ZonedDateTime occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.topic = topic;
        this.partition = partition;
        this.offset = offset;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }
}
