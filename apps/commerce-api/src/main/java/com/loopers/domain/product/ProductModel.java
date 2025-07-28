package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;

import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "product")
@Getter
public class ProductModel extends BaseEntity {

    private Long brandId;

    private Long categoryId;

    private String name;
    
    private String description;
    
    private int price;
    
    private int stock;
    
    private int likesCount;

    protected ProductModel() {}

    @Builder
    public ProductModel(Long brandId, Long categoryId, String name, String description, int price, int stock) {
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantity) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public void incrementLikesCount() {
        this.likesCount++;
    }

    public void decrementLikesCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }
}
