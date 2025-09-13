package com.loopers.application.product;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStockDomainService;
import com.loopers.domain.product.event.StockAdjustedEvent;
import com.loopers.domain.product.event.StockAdjustedPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class StockDeductionProcessor {

    private final ProductStockDomainService productStockDomainService;
    private final ProductRepository productRepository;
    private final StockAdjustedPublisher stockAdjustedPublisher;

    @Transactional
    public void deductProductStocks(List<OrderItemModel> orderItems) {
        orderItems.forEach(item -> {
            ProductModel product = productRepository.findByIdForUpdate(item.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. 상품 ID: " + item.getProductId()));

            // 재고 변경 전 상태 기록
            int oldStock = product.getStock();

            // 재고 차감
            productStockDomainService.deductStock(product, item.getQuantity());

            // 재고 변경 후 상태
            int newStock = product.getStock();

            // 상품 저장
            productRepository.save(product);

            // Kafka 이벤트 발행 (재고 변경 이벤트 - 캐시 무효화)
            stockAdjustedPublisher.publish(
                    StockAdjustedEvent.create(product.getId(), oldStock, newStock)
            );

            log.info("재고 차감 및 이벤트 발행 완료 - ProductId: {}, OldStock: {}, NewStock: {}",
                    product.getId(), oldStock, newStock);

        });
    }

    @Transactional
    public void restoreProductStocks(List<OrderItemModel> orderItems) {
        orderItems.forEach(item -> {
            ProductModel product = productRepository.findByIdForUpdate(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. 상품 ID: " + item.getProductId()));

            // 재고 복원 전 상태 기록
            int oldStock = product.getStock();

            // 재고 복원
            productStockDomainService.restoreStock(product, item.getQuantity());

            // 재고 복원 후 상태
            int newStock = product.getStock();

            // 상품 저장
            productRepository.save(product);

            // Kafka 이벤트 발행 (재고 변경 이벤트 - 캐시 무효화)
            stockAdjustedPublisher.publish(
                    StockAdjustedEvent.create(product.getId(), oldStock, newStock)
            );

            log.info("재고 복원 및 이벤트 발행 완료 - ProductId: {}, OldStock: {}, NewStock: {}",
                    product.getId(), oldStock, newStock);
        });
    }
}
