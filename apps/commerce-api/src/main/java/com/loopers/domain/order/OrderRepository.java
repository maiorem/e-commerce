package com.loopers.domain.order;

import com.loopers.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    
    OrderModel save(OrderModel order);
    
    Optional<OrderModel> findById(Long id);
    
    List<OrderModel> findByUserId(UserId userId);
    
    List<OrderModel> findAll();
    
    void delete(OrderModel order);
} 
