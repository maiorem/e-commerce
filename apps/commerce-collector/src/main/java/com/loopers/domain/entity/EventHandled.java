package com.loopers.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Entity
@Table(name = "event_handled")
public class EventHandled extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private ZonedDateTime processedAt;

    @Column(name = "consumer_group", nullable = false)
    private String consumerGroup;

    protected EventHandled() {}

    public static EventHandled of(String eventId, String consumerGroup) {
        EventHandled eventHandled = new EventHandled();
        eventHandled.eventId = eventId;
        eventHandled.consumerGroup = consumerGroup;
        eventHandled.processedAt = ZonedDateTime.now();
        return eventHandled;
    }

}
