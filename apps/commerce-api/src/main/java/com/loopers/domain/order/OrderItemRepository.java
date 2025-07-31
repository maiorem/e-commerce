package com.loopers.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository {
    
    OrderItemModel save(OrderItemModel orderItem);
    
    Optional<OrderItemModel> findById(Long id);
    
    List<OrderItemModel> findByOrderId(Long orderId);
    
    List<OrderItemModel> findByProductId(Long productId);
    
    void delete(OrderItemModel orderItem);
} 
