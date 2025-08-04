# 시퀀스 다이어그램

---

## 1. 좋아요 등록/취소 요청

```mermaid
sequenceDiagram
    actor User
    participant ProductV1Controller
    participant LikeApplicationService
    participant ProductLikeHandler
    participant ProductRepository
    participant LikeRepository

    User->>ProductV1Controller: (좋아요 등록/취소) /api/v1/products/{productId}/likes (X-USER-ID)
    activate ProductV1Controller
    ProductV1Controller->>LikeApplicationService: 상품 좋아요 등록/취소 요청 (userId, productId)
    activate LikeApplicationService
    LikeApplicationService->>ProductRepository: 상품 존재 여부 조회 (productId)
    activate ProductRepository
    ProductRepository-->>LikeApplicationService: 상품 조회 결과 전달
    deactivate ProductRepository

    alt 상품이 존재하지 않으면
        LikeApplicationService--xProductV1Controller: 오류 응답 반환 (404 Not Found)
        ProductV1Controller--xUser: 상품 없음 오류 반환 (404 Not Found)
    else 상품이 존재하면
        LikeApplicationService->>ProductLikeHandler: 좋아요 추가/제거 처리 (product, userId)
        activate ProductLikeHandler
        ProductLikeHandler->>LikeRepository: 기존 좋아요 존재 여부 확인 (userId, productId)
        activate LikeRepository
        LikeRepository-->>ProductLikeHandler: 좋아요 존재 여부 반환
        deactivate LikeRepository
        
        alt 좋아요 요청인 케이스
            ProductLikeHandler->>ProductLikeHandler: LikeModel 생성 및 상품 좋아요 수 증가
            ProductLikeHandler-->>LikeApplicationService: 생성된 LikeModel 반환
        else 취소 요청인 케이스
            ProductLikeHandler->>ProductLikeHandler: 기존 LikeModel 조회 및 상품 좋아요 수 감소
            ProductLikeHandler-->>LikeApplicationService: 제거할 LikeModel 반환
        end
        deactivate ProductLikeHandler
        
        alt LikeModel이 null이 아닌 경우 (실제 변경이 필요한 경우)
            LikeApplicationService->>LikeRepository: 좋아요 정보 저장/삭제
            activate LikeRepository
            LikeRepository-->>LikeApplicationService: 저장/삭제 결과 반환
            deactivate LikeRepository
            LikeApplicationService->>ProductRepository: 상품 정보 업데이트 (좋아요 수 변경)
            activate ProductRepository
            ProductRepository-->>LikeApplicationService: 상품 업데이트 결과 반환
            deactivate ProductRepository
        end
        
        LikeApplicationService->>ProductV1Controller: 좋아요 성공 처리 결과 반환
        deactivate LikeApplicationService
        ProductV1Controller-->>User: 좋아요 성공 처리 결과 전달
        deactivate ProductV1Controller
    end
```

---

## 2. 사용자가 좋아요 한 상품 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant UserV1Controller
    participant LikeApplicationService
    participant ProductLikeHandler
    participant ProductRepository
    participant BrandRepository
    participant CategoryRepository
    participant LikeRepository

    User->>UserV1Controller: GET /api/v1/users/{userId}/likes (X-USER-ID)
    activate UserV1Controller
    UserV1Controller->>LikeApplicationService: 사용자가 좋아요 한 상품 목록 조회 요청 (userId)
    activate LikeApplicationService
    LikeApplicationService->>ProductLikeHandler: 사용자 좋아요 상품 ID 목록 조회 (userId)
    activate ProductLikeHandler
    ProductLikeHandler->>LikeRepository: 사용자 정보로 좋아요 목록 요청 (userId)
    activate LikeRepository
    LikeRepository-->>ProductLikeHandler: 사용자 좋아요 목록 반환
    deactivate LikeRepository
    ProductLikeHandler-->>LikeApplicationService: 좋아요 상품 ID 목록 반환
    deactivate ProductLikeHandler
    
    alt 좋아요 상품이 존재하면
        LikeApplicationService->>ProductRepository: 상품 정보 일괄 조회 (productId 목록)
        activate ProductRepository
        ProductRepository-->>LikeApplicationService: 상품 정보 목록 반환
        deactivate ProductRepository
        
        LikeApplicationService->>BrandRepository: 브랜드 정보 조회 (brandId 목록)
        activate BrandRepository
        BrandRepository-->>LikeApplicationService: 브랜드 정보 반환
        deactivate BrandRepository
        
        LikeApplicationService->>CategoryRepository: 카테고리 정보 조회 (categoryId 목록)
        activate CategoryRepository
        CategoryRepository-->>LikeApplicationService: 카테고리 정보 반환
        deactivate CategoryRepository
        
        LikeApplicationService->>LikeApplicationService: ProductOutputInfo 목록 생성
    end
    
    LikeApplicationService->>UserV1Controller: 상품 목록 결과 반환
    deactivate LikeApplicationService
    UserV1Controller-->>User: 좋아요 누른 상품 목록 결과 전달
    deactivate UserV1Controller
```

---

## 3. 주문 생성 요청

```mermaid
sequenceDiagram
    actor User
    participant OrderV1Controller
    participant OrderApplicationService
    participant OrderCreationDomainService
    participant OrderUsePointDomainService
    participant ProductStockDomainService
    participant PointDomainService
    participant ExternalPaymentGatewayService
    participant ProductRepository
    participant PointRepository
    participant UserRepository
    participant OrderRepository
    participant OrderItemRepository
    participant PaymentRepository

    User->>OrderV1Controller: POST /api/v1/orders (userId, items, usePoints)
    activate OrderV1Controller
    OrderV1Controller->>OrderApplicationService: 주문 요청 (OrderCommand)
    activate OrderApplicationService
    
    OrderApplicationService->>UserRepository: 사용자 존재 여부 확인 (userId)
    activate UserRepository
    UserRepository-->>OrderApplicationService: 사용자 존재 여부 반환
    deactivate UserRepository
    
    OrderApplicationService->>ProductRepository: 주문 상품 정보 조회 (productIds)
    activate ProductRepository
    ProductRepository-->>OrderApplicationService: 상품 정보 목록 반환
    deactivate ProductRepository
    
    OrderApplicationService->>OrderCreationDomainService: 주문 아이템 유효성 검증 (orderItems, products)
    activate OrderCreationDomainService
    OrderCreationDomainService-->>OrderApplicationService: 검증 결과 반환
    deactivate OrderCreationDomainService
    
    OrderApplicationService->>OrderCreationDomainService: 주문 총액 계산 (orderItems)
    activate OrderCreationDomainService
    OrderCreationDomainService-->>OrderApplicationService: 총액 반환
    deactivate OrderCreationDomainService
    
    OrderApplicationService->>PointRepository: 사용자 포인트 잔액 조회 (userId)
    activate PointRepository
    PointRepository-->>OrderApplicationService: 포인트 정보 반환
    deactivate PointRepository
    
    OrderApplicationService->>OrderUsePointDomainService: 사용 포인트 계산 (availablePoint, orderPrice, usePoints)
    activate OrderUsePointDomainService
    OrderUsePointDomainService-->>OrderApplicationService: 사용할 포인트 금액 반환
    deactivate OrderUsePointDomainService
    
    alt 사용할 포인트가 있으면
        OrderApplicationService->>PointDomainService: 포인트 사용 처리 (availablePoint, usedPoints)
        activate PointDomainService
        PointDomainService-->>OrderApplicationService: 포인트 사용 결과 반환
        deactivate PointDomainService
    end
    
    OrderApplicationService->>OrderCreationDomainService: 최종 결제 금액 계산 (orderPrice, usedPoints)
    activate OrderCreationDomainService
    OrderCreationDomainService-->>OrderApplicationService: 최종 결제 금액 반환
    deactivate OrderCreationDomainService
    
    OrderApplicationService->>OrderApplicationService: OrderModel 생성 (userId, finalTotalPrice)
    
    OrderApplicationService->>ExternalPaymentGatewayService: 결제 처리 (order)
    activate ExternalPaymentGatewayService
    ExternalPaymentGatewayService-->>OrderApplicationService: 결제 결과 (PaymentHistoryModel) 반환
    deactivate ExternalPaymentGatewayService
    
    OrderApplicationService->>ProductStockDomainService: 재고 차감 처리 (products, orderItems)
    activate ProductStockDomainService
    ProductStockDomainService-->>OrderApplicationService: 재고 차감 결과 반환
    deactivate ProductStockDomainService
    
    OrderApplicationService->>OrderItemRepository: 주문 아이템 저장 (orderItems)
    activate OrderItemRepository
    OrderItemRepository-->>OrderApplicationService: 저장된 주문 아이템 반환
    deactivate OrderItemRepository
    
    OrderApplicationService->>OrderRepository: 주문 정보 저장 (order)
    activate OrderRepository
    OrderRepository-->>OrderApplicationService: 저장된 주문 정보 반환
    deactivate OrderRepository
    
    OrderApplicationService->>PaymentRepository: 결제 내역 저장 (paymentHistory)
    activate PaymentRepository
    PaymentRepository-->>OrderApplicationService: 결제 내역 저장 결과 반환
    deactivate PaymentRepository
    
    OrderApplicationService->>OrderApplicationService: OrderInfo 생성 (order, orderItemInfos)
    OrderApplicationService->>OrderV1Controller: 주문 성공 응답 반환 (OrderInfo)
    deactivate OrderApplicationService
    OrderV1Controller-->>User: 주문 성공 응답 전달
    deactivate OrderV1Controller
```

---

## 4. 쿠폰 조회

```mermaid
sequenceDiagram
    actor User
    participant CouponV1Controller
    participant CouponApplicationService
    participant CouponRepository
    participant UserRepository

    User->>CouponV1Controller: GET /api/v1/coupons (X-USER-ID)
    activate CouponV1Controller
    CouponV1Controller->>CouponApplicationService: 사용자 쿠폰 목록 조회 요청 (userId)
    activate CouponApplicationService
    
    CouponApplicationService->>UserRepository: 사용자 존재 여부 확인 (userId)
    activate UserRepository
    UserRepository-->>CouponApplicationService: 사용자 존재 여부 반환
    deactivate UserRepository
    
    alt 사용자가 존재하지 않으면
        CouponApplicationService--xCouponV1Controller: 오류 응답 반환 (401 Unauthorized)
        CouponV1Controller--xUser: 사용자 인증 오류 반환 (401 Unauthorized)
    else 사용자가 존재하면
        CouponApplicationService->>CouponRepository: 사용자 쿠폰 목록 조회 (userId)
        activate CouponRepository
        CouponRepository-->>CouponApplicationService: 쿠폰 목록 반환
        deactivate CouponRepository
        
        CouponApplicationService->>CouponApplicationService: 쿠폰 유효성 검증 (유효기간, 사용가능여부)
        CouponApplicationService->>CouponV1Controller: 쿠폰 목록 결과 반환
        deactivate CouponApplicationService
        CouponV1Controller-->>User: 쿠폰 목록 결과 전달
        deactivate CouponV1Controller
    end
```

---

## 5. 쿠폰 사용 (주문 시)

```mermaid
sequenceDiagram
    actor User
    participant OrderV1Controller
    participant OrderApplicationService
    participant CouponApplicationService
    participant CouponValidationDomainService
    participant CouponCalculationDomainService
    participant CouponRepository
    participant OrderCreationDomainService
    participant ProductRepository
    participant PointRepository
    participant UserRepository
    participant OrderRepository
    participant OrderItemRepository
    participant PaymentRepository

    User->>OrderV1Controller: POST /api/v1/orders (userId, items, usePoints, couponId)
    activate OrderV1Controller
    OrderV1Controller->>OrderApplicationService: 주문 요청 (OrderCommand with couponId)
    activate OrderApplicationService
    
    OrderApplicationService->>UserRepository: 사용자 존재 여부 확인 (userId)
    activate UserRepository
    UserRepository-->>OrderApplicationService: 사용자 존재 여부 반환
    deactivate UserRepository
    
    OrderApplicationService->>ProductRepository: 주문 상품 정보 조회 (productIds)
    activate ProductRepository
    ProductRepository-->>OrderApplicationService: 상품 정보 목록 반환
    deactivate ProductRepository
    
    OrderApplicationService->>OrderCreationDomainService: 주문 아이템 유효성 검증 (orderItems, products)
    activate OrderCreationDomainService
    OrderCreationDomainService-->>OrderApplicationService: 검증 결과 반환
    deactivate OrderCreationDomainService
    
    OrderApplicationService->>OrderCreationDomainService: 주문 총액 계산 (orderItems)
    activate OrderCreationDomainService
    OrderCreationDomainService-->>OrderApplicationService: 총액 반환
    deactivate OrderCreationDomainService
    
    alt 쿠폰이 사용되는 경우
        OrderApplicationService->>CouponRepository: 쿠폰 정보 조회 (couponId)
        activate CouponRepository
        CouponRepository-->>OrderApplicationService: 쿠폰 정보 반환
        deactivate CouponRepository
        
        OrderApplicationService->>CouponValidationDomainService: 쿠폰 유효성 검증 (coupon, userId, orderPrice)
        activate CouponValidationDomainService
        CouponValidationDomainService->>CouponValidationDomainService: 사용 가능 여부, 유효기간, 사용 이력 확인
        CouponValidationDomainService-->>OrderApplicationService: 쿠폰 유효성 검증 결과 반환
        deactivate CouponValidationDomainService
        
        alt 쿠폰이 유효하지 않으면
            OrderApplicationService--xOrderV1Controller: 쿠폰 사용 불가 오류 반환 (400 Bad Request)
            OrderV1Controller--xUser: 쿠폰 사용 불가 오류 반환 (400 Bad Request)
        else 쿠폰이 유효하면
            OrderApplicationService->>CouponCalculationDomainService: 쿠폰 할인 금액 계산 (coupon, orderPrice)
            activate CouponCalculationDomainService
            CouponCalculationDomainService->>CouponCalculationDomainService: 정액/정률 할인 계산
            CouponCalculationDomainService-->>OrderApplicationService: 할인 금액 반환
            deactivate CouponCalculationDomainService
            
            alt 할인 금액이 주문 금액을 초과하면
                OrderApplicationService--xOrderV1Controller: 할인 금액 초과 오류 반환 (400 Bad Request)
                OrderV1Controller--xUser: 할인 금액 초과 오류 반환 (400 Bad Request)
            else 할인 금액이 유효하면
                OrderApplicationService->>OrderCreationDomainService: 쿠폰 적용 후 최종 금액 계산 (orderPrice, discountAmount)
                activate OrderCreationDomainService
                OrderCreationDomainService-->>OrderApplicationService: 쿠폰 적용 후 금액 반환
                deactivate OrderCreationDomainService
            end
        end
    end
    
    OrderApplicationService->>PointRepository: 사용자 포인트 잔액 조회 (userId)
    activate PointRepository
    PointRepository-->>OrderApplicationService: 포인트 정보 반환
    deactivate PointRepository
    
    OrderApplicationService->>OrderUsePointDomainService: 사용 포인트 계산 (availablePoint, finalPrice, usePoints)
    activate OrderUsePointDomainService
    OrderUsePointDomainService-->>OrderApplicationService: 사용할 포인트 금액 반환
    deactivate OrderUsePointDomainService
    
    alt 사용할 포인트가 있으면
        OrderApplicationService->>PointDomainService: 포인트 사용 처리 (availablePoint, usedPoints)
        activate PointDomainService
        PointDomainService-->>OrderApplicationService: 포인트 사용 결과 반환
        deactivate PointDomainService
    end
    
    OrderApplicationService->>OrderCreationDomainService: 최종 결제 금액 계산 (finalPrice, usedPoints)
    activate OrderCreationDomainService
    OrderCreationDomainService-->>OrderApplicationService: 최종 결제 금액 반환
    deactivate OrderCreationDomainService
    
    OrderApplicationService->>OrderApplicationService: OrderModel 생성 (userId, finalTotalPrice, couponId)
    
    OrderApplicationService->>ExternalPaymentGatewayService: 결제 처리 (order)
    activate ExternalPaymentGatewayService
    ExternalPaymentGatewayService-->>OrderApplicationService: 결제 결과 (PaymentHistoryModel) 반환
    deactivate ExternalPaymentGatewayService
    
    OrderApplicationService->>ProductStockDomainService: 재고 차감 처리 (products, orderItems)
    activate ProductStockDomainService
    ProductStockDomainService-->>OrderApplicationService: 재고 차감 결과 반환
    deactivate ProductStockDomainService
    
    alt 쿠폰이 사용된 경우
        OrderApplicationService->>CouponRepository: 쿠폰 사용 상태 업데이트 (couponId, used)
        activate CouponRepository
        CouponRepository-->>OrderApplicationService: 쿠폰 상태 업데이트 결과 반환
        deactivate CouponRepository
    end
    
    OrderApplicationService->>OrderItemRepository: 주문 아이템 저장 (orderItems)
    activate OrderItemRepository
    OrderItemRepository-->>OrderApplicationService: 저장된 주문 아이템 반환
    deactivate OrderItemRepository
    
    OrderApplicationService->>OrderRepository: 주문 정보 저장 (order)
    activate OrderRepository
    OrderRepository-->>OrderApplicationService: 저장된 주문 정보 반환
    deactivate OrderRepository
    
    OrderApplicationService->>PaymentRepository: 결제 내역 저장 (paymentHistory)
    activate PaymentRepository
    PaymentRepository-->>OrderApplicationService: 결제 내역 저장 결과 반환
    deactivate PaymentRepository
    
    OrderApplicationService->>OrderApplicationService: OrderInfo 생성 (order, orderItemInfos, couponInfo)
    OrderApplicationService->>OrderV1Controller: 주문 성공 응답 반환 (OrderInfo)
    deactivate OrderApplicationService
    OrderV1Controller-->>User: 주문 성공 응답 전달
    deactivate OrderV1Controller
```