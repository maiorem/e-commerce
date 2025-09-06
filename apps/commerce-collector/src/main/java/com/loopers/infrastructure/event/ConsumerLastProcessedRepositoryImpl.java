package com.loopers.infrastructure.event;

import com.loopers.domain.entity.ConsumerLastProcessed;
import com.loopers.domain.repository.ConsumerLastProcessedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConsumerLastProcessedRepositoryImpl implements ConsumerLastProcessedRepository {

    private final ConsumerLastProcessedJpaRepository lastProcessedJpaRepository;


    @Override
    public Optional<ConsumerLastProcessed> findByConsumerGroupAndAggregateId(String consumerGroup, String aggregateId) {
        return lastProcessedJpaRepository.findByConsumerGroupAndAggregateId(consumerGroup, aggregateId);
    }

    @Override
    public ConsumerLastProcessed save(ConsumerLastProcessed entity) {
        return lastProcessedJpaRepository.save(entity);
    }
}
