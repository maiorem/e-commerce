package com.loopers.application.order;

import com.loopers.domain.order.*;
import com.loopers.domain.payment.ExternalPaymentGatewayService;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.point.*;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStockDomainService;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    private final PaymentRepository paymentRepository;

    private final OrderCreationDomainService orderCreationDomainService;
    private final OrderUsePointDomainService orderUsePointDomainService;

    private final ProductStockDomainService productStockDomainService;

    private final PointDomainService pointDomainService;

    private final ExternalPaymentGatewayService externalPaymentGatewayService;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderInfo createOrder(OrderCommand command) {

        if(!userRepository.existsByUserId(command.userId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다. 사용자 ID: " + command.userId());
        }

        // 주문 아이템 상품 전체 조회
        List<Long> productIds = command.items().stream()
            .map(OrderItemCommand::productId)
            .toList();
        List<ProductModel> products = productRepository.findAllByIds(productIds);

        List<OrderItemModel> orderItems = OrderItemCommand.convertToOrderItems(command.items());

        // 주문 아이템 유효성 검증
        orderCreationDomainService.validateOrderItems(orderItems, products);

        // 총액 계산
        int orderPrice = orderCreationDomainService.calculateOrderPrice(orderItems);

        // 내 포인트 잔액 확인
        PointModel availablePoint = pointRepository.findByUserId(command.userId()).orElse(null);

        // 주문에서 사용하기로 한 포인트 검증
        int usedPoints = orderUsePointDomainService.calculateUsePoint(availablePoint, orderPrice, command.usePoints());

        // 포인트 사용
        if (usedPoints > 0 && availablePoint != null) {
            availablePoint.use(usedPoints);
            pointRepository.save(availablePoint);

            // 포인트 사용 내역 저장
            PointHistoryModel pointHistory = pointDomainService.createPointHistory(command.userId(), usedPoints, availablePoint.getAmount(), PointChangeReason.ORDER);
            pointRepository.saveHistory(pointHistory);
        }

        // 최종 결제 금액
        int finalTotalPrice = orderCreationDomainService.calculateFinalTotalPrice(orderPrice, usedPoints);

        OrderModel order = OrderModel.of(command.userId(), finalTotalPrice);

        // 결제
        PaymentHistoryModel paymentHistory = externalPaymentGatewayService.processPayment(order);

        // 재고 차감
        orderItems.forEach(item -> {
            ProductModel product = products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + item.getProductId()));
            productStockDomainService.deductStock(product, item.getQuantity());
        });

        // 주문 저장 
        orderRepository.save(order);        
        
        // 주문 아이템에 실제 주문 ID 설정 후 저장
        List<OrderItemModel> savedOrderItems = new ArrayList<>();
        orderItems.forEach(item -> {
            OrderItemModel orderItemWithOrderId = OrderItemModel.builder()
                .orderId(order.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .priceAtOrder(item.getPriceAtOrder())
                .build();
            OrderItemModel savedItem = orderItemRepository.save(orderItemWithOrderId);

            savedOrderItems.add(savedItem);
        });


        // 결제 내역 저장
        paymentRepository.save(paymentHistory);

        List<OrderItemInfo> orderItemInfos = OrderItemInfo.createOrderItemInfos(savedOrderItems, products);
        return OrderInfo.from(order, orderItemInfos);
    }

} 
