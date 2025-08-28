package com.loopers.application.order;

import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.application.user.UserValidator;
import com.loopers.domain.order.OrderCreationDomainService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.event.*;
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

	private final StockDeductionProcessor stockDeductionProcessor;

	private final OrderPersistenceHandler orderPersistenceHandler;

	private final OrderCreationDomainService orderCreationDomainService;
	private final OrderPriceCalculator orderPriceCalculator;

	private final OrderCreatedPublisher orderCreatedPublisher;
    private final OrderCreatedCouponReservePublisher couponReservePublisher;
    private final OrderCreatedStockDeductionPublisher stockDeductionPublisher;

	/**
	 * 주문 생성
	 */
    @Transactional
	public OrderInfo createOrder(OrderCommand command) {
		// 1. 사용자 검증
		userValidator.validateUserExists(command.userId());

		// 2. 상품 검증 및 조회
		List<ProductModel> products = orderItemProductsValidator.validateAndGetProducts(command.items());
		List<OrderItemModel> orderItems = OrderItemCommand.convertToOrderItems(command.items());

		// 3. 가격 계산
		int orderPrice = orderCreationDomainService.calculateOrderPrice(orderItems);
		OrderPricing pricing = orderPriceCalculator.calculate(command.userId(), orderPrice, command.couponCode());

		// 4. 주문 생성 및 저장 (CREATED 상태)
		OrderModel order = OrderModel.create(command.userId(), pricing.getFinalAmount(), command.paymentMethod());
		OrderModel savedOrder = orderPersistenceHandler.saveOrder(order);

		// 5. 주문 아이템 저장
		List<OrderItemModel> savedOrderItems = orderPersistenceHandler.saveOrderItem(savedOrder, orderItems);

		// -- 주문생성 이벤트 발행 --
        orderCreatedPublisher.publish(OrderCreatedEvent.from(savedOrder));
        // -- 쿠폰예약 커맨드 발행 --
        couponReservePublisher.publish(OrderCeatedCouponReserveCommand.create(savedOrder.getId(), savedOrder.getUserId(), command.couponCode()));
        // -- 재고차감 커맨드 발행 --
        stockDeductionPublisher.publish(OrderCreatedStockDeductionCommand.create(orderItems));

		return OrderInfo.from(savedOrder, OrderItemInfo.createOrderItemInfos(savedOrderItems, products));
	}

} 
