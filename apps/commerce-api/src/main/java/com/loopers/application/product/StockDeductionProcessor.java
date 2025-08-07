package com.loopers.application.product;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStockDomainService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class StockDeductionProcessor {

    private final ProductStockDomainService productStockDomainService;
    private final ProductRepository productRepository;

    @Transactional
    public void deductProductStocks(List<OrderItemModel> orderItems) {
        orderItems.forEach(item -> {
            ProductModel product = productRepository.findByIdForUpdate(item.getProductId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. 상품 ID: " + item.getProductId()));
            productStockDomainService.deductStock(product, item.getQuantity());
        });
    }
}
