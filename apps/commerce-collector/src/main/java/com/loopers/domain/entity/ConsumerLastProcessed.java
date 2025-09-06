package com.loopers.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "consumer_last_processed")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsumerLastProcessed extends BaseEntity {

    @Column(name = "consumer_group", nullable = false)
    private String consumerGroup;

    @Column(name = "aggregate_id", nullable = false) // productId 등
    private String aggregateId;

    @Column(name = "last_processed_at", nullable = false)
    private ZonedDateTime lastProcessedAt;

    public static ConsumerLastProcessed of(String consumerGroup, String aggregateId, ZonedDateTime processedAt) {
        ConsumerLastProcessed entity = new ConsumerLastProcessed();
        entity.consumerGroup = consumerGroup;
        entity.aggregateId = aggregateId;
        entity.lastProcessedAt = processedAt;
        return entity;
    }

    public boolean shouldProcess(ZonedDateTime eventTime) {
        return eventTime.isAfter(this.lastProcessedAt);
    }

    public void updateLastProcessedAt(ZonedDateTime eventTime) {
        this.lastProcessedAt = eventTime;
    }
}
