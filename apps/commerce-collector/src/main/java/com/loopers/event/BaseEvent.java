package com.loopers.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public abstract class BaseEvent {
    
    private final String eventId;
    private final ZonedDateTime occurredAt;
    
    protected BaseEvent(String eventId) {
        this.eventId = eventId;
        this.occurredAt = ZonedDateTime.now();
    }
}