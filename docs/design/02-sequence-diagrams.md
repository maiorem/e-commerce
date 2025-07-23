# 시퀀스 다이어그램

---

## 1.좋아요 등록/취소 요청

```mermaid
sequenceDiagram
    actor User
    participant LikeV1Controller
    participant LikeFacade
    participant LikeService
    participant ProductService
    participant LikeRepository
    participant ProductRepository

    User->>LikeV1Controller: (좋아요 등록/취소) /api/v1/products/{productId}/likes (X-USER-ID)
    activate LikeV1Controller
    LikeV1Controller->>LikeFacade: 상품 좋아요 등록/취소 요청 (userId, productId)
    activate LikeFacade
    LikeFacade->>LikeService: 상품 좋아요 등록/취소 요청 (userId, productId)
    activate LikeService

    LikeService->>ProductService: 해당 상품 존재 여부 조회 (productId)
    activate ProductService
    ProductService->>ProductRepository: 해당 상품 조회 (productId)
    activate ProductRepository
    ProductRepository-->>ProductService: 상품 조회 결과 전달
    deactivate ProductRepository
    ProductService-->>LikeService: 상품 존재 여부 결과
    deactivate ProductService

    alt 상품이 존재하지 않으면
        LikeService--xLikeFacade: 상품 없음 오류 (404 Not Found)
        LikeFacade--xLikeV1Controller: 오류 응답 반환 (404 Not Found)
        LikeV1Controller--xUser: 상품 없음 오류 반환 (404 Not Found)
    else 상품이 존재하면
        LikeService->>LikeRepository: 기존 좋아요 정보가 존재하는지 확인 (userId, productId)
        activate LikeRepository
        LikeRepository-->>LikeService: 좋아요 존재 여부 반환
        deactivate LikeRepository
        alt 좋아요 요청인 케이스
            alt 좋아요가 이미 등록되어 있는 경우 
                LikeService-->>LikeFacade: 성공 처리
            else 좋아요가 등록되지 않은 경우
                LikeService->>LikeRepository: 좋아요 생성 (userId, productId)
                activate LikeRepository
                LikeRepository-->>LikeService: 좋아요 생성 결과
                deactivate LikeRepository
                LikeService->>ProductService: 상품에 좋아요 수 증가 요청 (productId)
                activate ProductService
                ProductService->>ProductRepository: 상품 좋아요 수 증가 요청 (productId)
                activate ProductRepository
                ProductRepository-->>ProductService: 좋아요 수 업데이트 결과 반환
                deactivate ProductRepository
                ProductService-->>LikeService: 좋아요 수 업데이트 결과 반환
                deactivate ProductService
                LikeService-->>LikeFacade: 좋아요 성공 처리
            end
        else 취소 요청인 케이스
            alt 좋아요가 등록되어 있는 경우
                LikeService->>LikeRepository: 좋아요 삭제 요청 (userId, productId)
                activate LikeRepository
                LikeRepository-->>LikeService: 좋아요 삭제 결과
                deactivate LikeRepository
                LikeService->>ProductService: 상품에 좋아요 수 감소 요청 (productId)
                activate ProductService
                ProductService->>ProductRepository: 상품 좋아요 수 감소 요청 (productId)
                activate ProductRepository
                ProductRepository-->>ProductService: 좋아요 수 업데이트 결과 반환
                deactivate ProductRepository
                ProductService-->>LikeService: 좋아요 수 업데이트 결과 반환
                deactivate ProductService
                LikeService-->>LikeFacade: 좋아요 취소 성공 처리
            else 좋아요가 등록되지 않은 경우
                LikeService-->>LikeFacade: 성공 처리 (이미 취소된 상태)
            end
        end
        deactivate LikeService
        LikeFacade->>LikeV1Controller: 좋아요 성공 처리 결과 반환
        deactivate LikeFacade
        LikeV1Controller-->>User: 좋아요 성공 처리 결과 전달
        deactivate LikeV1Controller
    end
```

---

## 2. 사용자가 좋아요 한 상품 목록 조회

```mermaid
sequenceDiagram
    actor  User
    participant UserLikesV1Controller
    participant UserLikesFacade
    participant UserLikesService
    participant LikeRepository
    participant ProductRepository

    User->>UserLikesV1Controller: GET /api/v1/users/{userId}/likes (X-USER-ID)
    activate UserLikesV1Controller
    UserLikesV1Controller->>UserLikesFacade: 사용자가 좋아요 한 상품 목록 조회 요청 (userId)
    activate UserLikesFacade
    UserLikesFacade->>UserLikesService: 사용자가 좋아요 한 상품 목록 조회 요청 (userId)
    activate UserLikesService

    UserLikesService->>LikeRepository: 사용자 정보로 좋아요 목록 요청 (userId)
    activate LikeRepository
    LikeRepository-->>UserLikesService: 사용자 좋아요 목록 요청
    deactivate LikeRepository

    loop for each Like Record
        UserLikesService->>ProductRepository: 상품 조회 요청 (productId)
        activate ProductRepository
        ProductRepository-->>UserLikesService: 상품 정보 반환
        deactivate ProductRepository
    end

    UserLikesService-->>UserLikesFacade: 상품 목록 반환
    deactivate UserLikesService
    UserLikesFacade->>UserLikesV1Controller: 상품 목록 결과 반환
    deactivate UserLikesFacade
    UserLikesV1Controller-->>User: 좋아요 누른 상품 목력 결과 전달
    deactivate UserLikesV1Controller
```

---

## 3. 주문 생성 요청

```mermaid
sequenceDiagram
    actor User
    participant OrderV1Controller
    participant OrderFacade
    participant OrderService
    participant ProductService
    participant PointService
    participant ProductRepository
    participant PointRepository
    participant OrderRepository
    participant OrderItemRepository
    participant PG as 외부 결제 시스템 (Mock)

    User->>OrderV1Controller: POST /api/v1/orders (userId, items)
    activate OrderV1Controller
    OrderV1Controller->>OrderFacade: 주문 요청 (userId, orderRequestDto)
    activate OrderFacade
    OrderFacade->>OrderService: 주문 생성 처리 (userId, orderItems)
    activate OrderService

    loop 각 주문 상품별 재고 확인 및 차감
        OrderService->>ProductService: 재고 확인 및 차감 요청 (productId, quantity)
        activate ProductService
        ProductService->>ProductRepository: 상품 정보 조회 (productId)
        activate ProductRepository
        ProductRepository-->>ProductService: 상품 정보 반환
        deactivate ProductRepository
        ProductService->>ProductRepository: 재고 차감 요청 (productId, quantity)
        activate ProductRepository
        ProductRepository-->>ProductService: 재고 차감 결과 반환
        deactivate ProductRepository
        ProductService-->>OrderService: 재고 차감 처리 결과
        deactivate ProductService
        alt 재고 부족 시
            OrderService--xOrderFacade: 재고 부족 오류 (롤백)
            OrderFacade--xOrderV1Controller: 오류 응답 반환 (400 Bad Request)
            OrderV1Controller--xUser: 재고 부족 오류 전달
        end
    end

    OrderService->>PointService: 포인트 확인 및 차감 요청 (userId, totalAmount)
    activate PointService
    PointService->>PointRepository: 사용자 포인트 조회 (userId)
    activate PointRepository
    PointRepository-->>PointService: 포인트 정보 반환
    deactivate PointRepository
    PointService->>PointRepository: 포인트 차감 요청 (userId, amount)
    activate PointRepository
    PointRepository-->>PointService: 포인트 차감 결과
    deactivate PointRepository
    PointService-->>OrderService: 포인트 차감 처리 결과
    deactivate PointService
    alt 포인트 부족 시
        OrderService--xOrderFacade: 포인트 부족 오류 (롤백)
        OrderFacade--xOrderV1Controller: 오류 응답 반환 (400 Bad Request)
        OrderV1Controller--xUser: 포인트 부족 오류 전달
    end

    OrderService->>OrderRepository: 주문 정보 생성 및 저장 (orderModel)
    activate OrderRepository
    OrderRepository-->>OrderService: 생성된 주문 정보 반환
    deactivate OrderRepository

    loop 각 주문 상품 정보 저장
        OrderService->>OrderItemRepository: 주문 상품 정보 저장 (orderItemModel)
        activate OrderItemRepository
        OrderItemRepository-->>OrderService: 저장된 주문 상품 정보 반환
        deactivate OrderItemRepository
    end

    OrderService->>PG: 주문 정보 외부 전송 (orderModel)
    activate PG
    PG-->>OrderService: 전송 결과 반환
    deactivate PG
    OrderService-->>OrderFacade: 주문 생성 결과 (OrderInfo)
    deactivate OrderService
    OrderFacade->>OrderV1Controller: 주문 성공 응답 변환 (OrderResponse)
    deactivate OrderFacade
    OrderV1Controller-->>User: 주문 성공 응답 전달
    deactivate OrderV1Controller
```