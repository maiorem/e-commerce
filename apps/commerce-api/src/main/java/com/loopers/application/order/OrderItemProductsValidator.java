package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderItemProductsValidator {

    private final ProductRepository productRepository;

    public List<ProductModel> validateAndGetProducts(List<OrderItemCommand> items) {
        List<Long> productIds = items.stream()
                .map(OrderItemCommand::productId)
                .toList();
        List<ProductModel> products = productRepository.findAllByIds(productIds);
        List<OrderItemModel> orderItems = OrderItemCommand.convertToOrderItems(items);

        for (OrderItemModel orderItem : orderItems) {
            ProductModel productModel = products.stream()
                    .filter(product -> product.getId().equals(orderItem.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. 상품 ID: " + orderItem.getProductId()));
            validateOrderItem(orderItem, productModel);
        }

        return products;
    }

    private void validateOrderItem(OrderItemModel orderItem, ProductModel product) {
        if (orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다. 상품 ID: " + orderItem.getProductId());
        }

        if (product.getStock() < orderItem.getQuantity()) {
            throw new IllegalArgumentException(String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d, 상품 ID: %d",
                    product.getStock(), orderItem.getQuantity(), orderItem.getProductId()));
        }
    }
}
