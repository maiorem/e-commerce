# 보상 트랜잭션(Saga Pattern) 설계

---

## 1. 주문 상태 정의

```java
public enum OrderStatus {
    CREATED,           // 주문 생성됨
    STOCK_RESERVED,    // 재고 예약됨
    POINT_RESERVED,    // 포인트 예약됨
    COUPON_RESERVED,   // 쿠폰 예약됨
    PAYMENT_PROCESSING, // 결제 처리 중
    COMPLETED,         // 주문 완료
    CANCELLED,         // 주문 취소됨
    FAILED            // 주문 실패
}
```

---

## 2. Saga 단계별 처리

### **Step 1: 주문 생성**
```java
@Transactional
public OrderInfo createOrder(OrderCommand command) {
    // 1. 검증
    userValidator.validateUserExists(command.userId());
    List<ProductModel> products = orderItemProductsValidator.validateAndGetProducts(command.items());
    
    // 2. 주문 생성 (CREATED 상태)
    OrderModel order = OrderModel.create(command.userId(), command.items());
    orderRepository.save(order);
    
    // 3. 다음 단계 트리거
    orderSagaOrchestrator.startOrderSaga(order.getId());
    
    return OrderInfo.from(order);
}
```

### **Step 2: 재고 예약**
```java
@Transactional
public void reserveStock(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    try {
        // 재고 예약 처리
        stockReservationService.reserveStock(order.getOrderItems());
        
        // 주문 상태 업데이트
        order.updateStatus(OrderStatus.STOCK_RESERVED);
        orderRepository.save(order);
        
        // 다음 단계 트리거
        orderSagaOrchestrator.proceedToNextStep(orderId, "POINT_RESERVATION");
        
    } catch (InsufficientStockException e) {
        // 재고 부족 시 주문 취소
        order.updateStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        throw e;
    }
}
```

### **Step 3: 포인트 예약**
```java
@Transactional
public void reservePoints(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    try {
        // 포인트 예약 처리
        pointReservationService.reservePoints(order.getUserId(), order.getUsePoints());
        
        // 주문 상태 업데이트
        order.updateStatus(OrderStatus.POINT_RESERVED);
        orderRepository.save(order);
        
        // 다음 단계 트리거
        orderSagaOrchestrator.proceedToNextStep(orderId, "COUPON_RESERVATION");
        
    } catch (InsufficientPointsException e) {
        // 포인트 부족 시 보상 트랜잭션 실행
        compensateStockReservation(orderId);
        order.updateStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        throw e;
    }
}
```

### **Step 4: 쿠폰 예약**
```java
@Transactional
public void reserveCoupon(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    try {
        // 쿠폰 예약 처리
        couponReservationService.reserveCoupon(order.getUserId(), order.getCouponCode());
        
        // 주문 상태 업데이트
        order.updateStatus(OrderStatus.COUPON_RESERVED);
        orderRepository.save(order);
        
        // 다음 단계 트리거
        orderSagaOrchestrator.proceedToNextStep(orderId, "PAYMENT_PROCESSING");
        
    } catch (InvalidCouponException e) {
        // 쿠폰 오류 시 보상 트랜잭션 실행
        compensatePointReservation(orderId);
        compensateStockReservation(orderId);
        order.updateStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        throw e;
    }
}
```

### **Step 5: 결제 처리**
```java
@Transactional
public void processPayment(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    try {
        // 결제 처리
        PaymentHistoryModel paymentHistory = paymentProcessor.pay(order);
        
        // 주문 완료 처리
        order.complete(paymentHistory);
        orderRepository.save(order);
        
        // 모든 예약을 실제 사용으로 변경
        confirmAllReservations(orderId);
        
    } catch (PaymentFailedException e) {
        // 결제 실패 시 보상 트랜잭션 실행
        compensateCouponReservation(orderId);
        compensatePointReservation(orderId);
        compensateStockReservation(orderId);
        order.updateStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        throw e;
    }
}
```

---

## 3. 보상 트랜잭션 (Compensation)

### **재고 예약 취소**
```java
@Transactional
public void compensateStockReservation(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    // 재고 예약 취소
    stockReservationService.cancelReservation(order.getOrderItems());
    
    log.info("Stock reservation cancelled for order: {}", orderId);
}
```

### **포인트 예약 취소**
```java
@Transactional
public void compensatePointReservation(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    // 포인트 예약 취소
    pointReservationService.cancelReservation(order.getUserId(), order.getUsePoints());
    
    log.info("Point reservation cancelled for order: {}", orderId);
}
```

### **쿠폰 예약 취소**
```java
@Transactional
public void compensateCouponReservation(Long orderId) {
    OrderModel order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    
    // 쿠폰 예약 취소
    couponReservationService.cancelReservation(order.getUserId(), order.getCouponCode());
    
    log.info("Coupon reservation cancelled for order: {}", orderId);
}
```

---

## 4. Saga 오케스트레이터

```java
@Component
@RequiredArgsConstructor
public class OrderSagaOrchestrator {
    
    private final StockReservationService stockReservationService;
    private final PointReservationService pointReservationService;
    private final CouponReservationService couponReservationService;
    private final PaymentProcessingService paymentProcessingService;
    
    @Async
    public void startOrderSaga(Long orderId) {
        try {
            // Step 1: 재고 예약
            stockReservationService.reserveStock(orderId);
            
        } catch (Exception e) {
            log.error("Order saga failed at stock reservation step: {}", orderId, e);
            // 주문 상태를 FAILED로 변경
        }
    }
    
    @Async
    public void proceedToNextStep(Long orderId, String nextStep) {
        try {
            switch (nextStep) {
                case "POINT_RESERVATION":
                    pointReservationService.reservePoints(orderId);
                    break;
                case "COUPON_RESERVATION":
                    couponReservationService.reserveCoupon(orderId);
                    break;
                case "PAYMENT_PROCESSING":
                    paymentProcessingService.processPayment(orderId);
                    break;
            }
        } catch (Exception e) {
            log.error("Order saga failed at step {}: {}", nextStep, orderId, e);
            // 보상 트랜잭션 실행
            executeCompensation(orderId, nextStep);
        }
    }
    
    private void executeCompensation(Long orderId, String failedStep) {
        switch (failedStep) {
            case "POINT_RESERVATION":
                stockReservationService.compensateStockReservation(orderId);
                break;
            case "COUPON_RESERVATION":
                pointReservationService.compensatePointReservation(orderId);
                stockReservationService.compensateStockReservation(orderId);
                break;
            case "PAYMENT_PROCESSING":
                couponReservationService.compensateCouponReservation(orderId);
                pointReservationService.compensatePointReservation(orderId);
                stockReservationService.compensateStockReservation(orderId);
                break;
        }
    }
}
```

---

## 5. 예약 서비스 구현

### **재고 예약 서비스**
```java
@Service
@RequiredArgsConstructor
public class StockReservationService {
    
    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;
    
    @Transactional
    public void reserveStock(List<OrderItemModel> orderItems) {
        orderItems.forEach(item -> {
            ProductModel product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            
            // 재고 예약 (실제 차감이 아닌 예약)
            StockReservationModel reservation = StockReservationModel.create(
                product.getId(), 
                item.getQuantity()
            );
            stockReservationRepository.save(reservation);
            
            // 상품의 예약 가능 재고 차감
            product.reserveStock(item.getQuantity());
            productRepository.save(product);
        });
    }
    
    @Transactional
    public void cancelReservation(List<OrderItemModel> orderItems) {
        orderItems.forEach(item -> {
            ProductModel product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
            
            // 예약 취소
            stockReservationRepository.deleteByProductIdAndQuantity(
                product.getId(), 
                item.getQuantity()
            );
            
            // 상품의 예약 가능 재고 복원
            product.cancelStockReservation(item.getQuantity());
            productRepository.save(product);
        });
    }
}
```

---

## 6. 장점과 단점

### **장점**
- 긴 트랜잭션을 작은 단위로 분할
- 각 단계별 독립적인 트랜잭션 관리
- 실패 시 이전 단계들을 안전하게 롤백
- 시스템 가용성 향상

### **단점**
- 복잡성 증가
- 구현 및 테스트 어려움
- 일관성 보장이 어려움 (Eventual Consistency)
- 디버깅이 복잡

---

## 7. 실제 적용 시 고려사항

### **1. 이벤트 기반 처리**
```java
// 주문 상태 변경 이벤트 발행
@EventListener
public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
    // 다음 단계 자동 트리거
    orderSagaOrchestrator.proceedToNextStep(event.getOrderId(), event.getNextStep());
}
```

### **2. 타임아웃 처리**
```java
@Scheduled(fixedDelay = 30000) // 30초마다 실행
public void checkTimeoutReservations() {
    List<OrderModel> timeoutOrders = orderRepository.findTimeoutOrders();
    timeoutOrders.forEach(order -> {
        // 타임아웃된 예약들 취소
        executeCompensation(order.getId(), order.getCurrentStep());
    });
}
```

### **3. 멱등성 보장**
```java
public void reserveStock(Long orderId) {
    // 이미 예약된 주문인지 확인
    if (orderRepository.existsByOrderIdAndStatus(orderId, OrderStatus.STOCK_RESERVED)) {
        return; // 이미 처리됨
    }
    // 예약 처리
}
```

