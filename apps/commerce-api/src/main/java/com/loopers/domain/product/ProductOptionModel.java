package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "product_option")
@Getter
public class ProductOptionModel extends BaseEntity {

    private Long productId;

    private String attributeName;
    private String attributeValue;
    private int optionStock;

    protected ProductOptionModel() {}

    @Builder
    public ProductOptionModel(Long productId, String attributeName, String attributeValue, int optionStock) {
        this.productId = productId;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.optionStock = optionStock;
    }

    public void deductOptionStock(int quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }
        if (this.optionStock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        this.optionStock -= quantity;
    }
} 
