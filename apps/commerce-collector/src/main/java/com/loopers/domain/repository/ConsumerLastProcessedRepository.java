package com.loopers.domain.repository;

import com.loopers.domain.entity.ConsumerLastProcessed;

import java.util.Optional;

public interface ConsumerLastProcessedRepository {
    Optional<ConsumerLastProcessed> findByConsumerGroupAndAggregateId(String consumerGroup, String aggregateId);
    ConsumerLastProcessed save(ConsumerLastProcessed entity);
}
