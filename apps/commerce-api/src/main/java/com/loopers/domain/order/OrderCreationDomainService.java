package com.loopers.domain.order;


import com.loopers.domain.product.ProductModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderCreationDomainService {

    /**
     * 주문 아이템 리스트 검증
     */
    public void validateOrderItems(List<OrderItemModel> orderItems, List<ProductModel> products) {
        for (OrderItemModel orderItem : orderItems) {
            ProductModel productModel = products.stream()
                    .filter(product -> product.getId().equals(orderItem.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + orderItem.getProductId()));
            validateOrderItem(orderItem, productModel);
        }
    }

    private void validateOrderItem(OrderItemModel orderItem, ProductModel product) {
        if (orderItem.getQuantity() <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 수량은 1 이상이어야 합니다. 상품 ID: " + orderItem.getProductId());
        }

        if (product.getStock() < orderItem.getQuantity()) {
            throw new CoreException(ErrorType.BAD_REQUEST,
                String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d, 상품 ID: %d",
                product.getStock(), orderItem.getQuantity(), orderItem.getProductId()));
        }
    }

    /**
     * 주문 가격 계산
     */
    public int calculateOrderPrice(List<OrderItemModel> orderItems) {
        return orderItems.stream()
                .mapToInt(orderItem -> orderItem.getPriceAtOrder() * orderItem.getQuantity())
                .sum();
    }

    /**
     * 최종 결제 금액 계산
     */
    public int calculateFinalTotalPrice(int orderPrice, int usedPoints) {
        return orderPrice - usedPoints;
    }
}
