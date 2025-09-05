package com.loopers.infrastructure.event;

import com.loopers.domain.entity.ConsumerLastProcessed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumerLastProcessedJpaRepository extends JpaRepository<ConsumerLastProcessed, Long> {
    Optional<ConsumerLastProcessed> findByConsumerGroupAndAggregateId(String consumerGroup, String aggregateId);
}
