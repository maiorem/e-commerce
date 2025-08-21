package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.user.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderJpaRepository extends JpaRepository<OrderModel, Long> {
    
    List<OrderModel> findByUserId(UserId userId);

    List<OrderModel> findByStatusAndCreatedAtBefore(OrderStatus orderStatus, LocalDateTime localDateTime);
}
