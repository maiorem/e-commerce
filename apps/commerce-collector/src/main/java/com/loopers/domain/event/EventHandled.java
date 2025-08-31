package com.loopers.domain.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event_handled")
public class EventHandled {

    @Id
    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "handled_at", nullable = false)
    private LocalDateTime handledAt;

    protected EventHandled() {}

    public EventHandled(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.handledAt = LocalDateTime.now();
    }

    public static EventHandled create(String eventId, String eventType) {
        return new EventHandled(eventId, eventType);
    }

}
