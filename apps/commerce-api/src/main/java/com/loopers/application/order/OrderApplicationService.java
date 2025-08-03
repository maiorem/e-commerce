package com.loopers.application.order;

import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.application.user.UserValidator;
import com.loopers.domain.order.OrderCreationDomainService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.payment.ExternalPaymentGatewayService;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final UserValidator userValidator;

    private final OrderItemProductsValidator orderItemProductsValidator;

    private final PointProcessor pointProcessor;

    private final StockDeductionProcessor stockDeductionProcessor;

    private final OrderPersistenceHandler orderPersistenceHandler;

    private final OrderCreationDomainService orderCreationDomainService;

    private final ExternalPaymentGatewayService externalPaymentGatewayService;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderInfo createOrder(OrderCommand command) { // OrderCommand는 Application Layer DTO

        // 1. 사용자 존재 여부 확인
        userValidator.validateUserExists(command.userId());

        // 2. 주문 아이템 상품 유효성 검증 및 상품 목록 조회
        List<ProductModel> products = orderItemProductsValidator.validateAndGetProducts(command.items());
        List<OrderItemModel> orderItems = OrderItemCommand.convertToOrderItems(command.items());

        // 3. 총액 계산
        int orderPrice = orderCreationDomainService.calculateOrderPrice(orderItems);

        // 4. 사용 요청한 포인트 처리
        int usedPoints = pointProcessor.processPointUsage(command.userId(), orderPrice, command.usePoints());

        // 5. 최종 결제 금액 계산 (주문 총액 - 사용 포인트)
        int finalTotalPrice = orderCreationDomainService.calculateFinalTotalPrice(orderPrice, usedPoints);

        // 6. 주문 엔티티 생성
        OrderModel order = OrderModel.create(command.userId(), finalTotalPrice);

        // 7. 결제 처리 및 이력 저장
        PaymentHistoryModel paymentHistory = externalPaymentGatewayService.processPayment(order);

        // 8. 재고 차감
        stockDeductionProcessor.deductProductStocks(orderItems, products);

        // 9. 주문 정보 및 주문 아이템 저장
        List<OrderItemModel> savedOrderItems = orderPersistenceHandler.saveOrderAndItems(order, orderItems, paymentHistory);

        // 10. 최종 응답 DTO 변환 및 반환
        List<OrderItemInfo> orderItemInfos = OrderItemInfo.createOrderItemInfos(savedOrderItems, products);
        return OrderInfo.from(order, orderItemInfos);
    }


} 
