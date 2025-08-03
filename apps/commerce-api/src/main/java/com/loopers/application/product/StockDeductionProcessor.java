package com.loopers.application.product;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStockDomainService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class StockDeductionProcessor {

    private final ProductStockDomainService productStockDomainService;

    public void deductProductStocks(List<OrderItemModel> orderItems, List<ProductModel> products) {
        orderItems.forEach(item -> {
            ProductModel product = products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + item.getProductId()));
            productStockDomainService.deductStock(product, item.getQuantity());

        });
    }
}
