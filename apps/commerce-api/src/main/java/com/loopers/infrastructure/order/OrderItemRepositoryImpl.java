package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public OrderItemModel save(OrderItemModel orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItemModel> findById(Long id) {
        return orderItemJpaRepository.findById(id);
    }

    @Override
    public List<OrderItemModel> findByOrderId(Long orderId) {
        return orderItemJpaRepository.findAllByOrderId(orderId);
    }

    @Override
    public List<OrderItemModel> findByProductId(Long productId) {
        return orderItemJpaRepository.findAllByProductId(productId);
    }

    @Override
    public void delete(OrderItemModel orderItem) {
        orderItemJpaRepository.delete(orderItem);
    }
}
