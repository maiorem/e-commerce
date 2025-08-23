package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public OrderModel save(OrderModel order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<OrderModel> findById(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public List<OrderModel> findByUserId(UserId userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public List<OrderModel> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public void delete(OrderModel order) {
        orderJpaRepository.delete(order);
    }

    @Override
    public List<OrderModel> findByStatusAndCreatedBefore(OrderStatus orderStatus, LocalDateTime localDateTime) {
        return orderJpaRepository.findByStatusAndCreatedAtBefore(orderStatus, localDateTime);
    }
}
