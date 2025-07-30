package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
            throw new CoreException(ErrorType.BAD_REQUEST, "차감할 수량은 0보다 커야 합니다.");
        }

        if (product.getStock() < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, 
                String.format("재고가 부족합니다. 현재 재고: %d, 요청 수량: %d", product.getStock(), quantity));
        }
    }

    /**
     * 재고 차감
     */
    public void deductStock(ProductModel product, int quantity) {
        validateStockDeduction(product, quantity);
        product.deductStock(quantity);
    }

} 