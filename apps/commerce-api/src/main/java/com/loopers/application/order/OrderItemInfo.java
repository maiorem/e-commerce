package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.List;

public record OrderItemInfo(
        Long productId,
        String productName,
        int quantity,
        int priceAtOrder
) {
    public static OrderItemInfo create(OrderItemModel orderItemModel, ProductModel productModel) {
        return new OrderItemInfo(
                productModel.getId(),
                productModel.getName(),
                orderItemModel.getQuantity(),
                orderItemModel.getPriceAtOrder()
        );
    }

    public static List<OrderItemInfo> createOrderItemInfos(List<OrderItemModel> orderItems, List<ProductModel> products) {
        return orderItems.stream()
                .filter(item -> item != null) // null 체크 추가
                .map(item -> {
                    ProductModel product = products.stream()
                            .filter(p -> p.getId().equals(item.getProductId()))
                            .findFirst()
                            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + item.getProductId()));
                    return OrderItemInfo.create(item, product);
                }).toList();

    }
}
