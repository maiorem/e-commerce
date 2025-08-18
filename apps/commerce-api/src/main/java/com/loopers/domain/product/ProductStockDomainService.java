package com.loopers.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductStockDomainService {

    /**
     * 재고 차감 가능 여부 검증
     */
    public void validateStockDeduction(ProductModel product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", product.getStock(), quantity));
        }
    }

    /**
     * 재고 차감
     */
    public void deductStock(ProductModel product, int quantity) {
        validateStockDeduction(product, quantity);
        product.deductStock(quantity);
    }

    /**
     * 재고 복원 가능 여부 검증
     */
    private void validateStockRestored(ProductModel product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("복원할 수량은 0보다 커야 합니다.");
        }

        if (product.getStock() + quantity < 0) {
            throw new IllegalArgumentException(String.format("재고 복원 후 재고가 음수가 될 수 없습니다. 현재 재고: %d, 복원 수량: %d", product.getStock(), quantity));
        }
    }

    /**
     * 재고 복원
     */
    public void restoreStock(ProductModel product, int quantity) {
        validateStockRestored(product, quantity);
        product.restoreStock(quantity);
    }

}
