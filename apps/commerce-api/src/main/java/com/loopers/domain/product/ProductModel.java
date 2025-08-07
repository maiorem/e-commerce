package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    public ProductModel(Long brandId, Long categoryId, String name, String description, int price, int stock, int likesCount) {
        this.brandId = brandId;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.likesCount = likesCount;
    }

    public void deductStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    public void incrementLikesCount() {
        this.likesCount++;
    }

    public void decrementLikesCount() {
        if (this.likesCount <= 0) { // 현재 좋아요 수가 0이거나 그보다 작을 때 감소 시도
            throw new IllegalArgumentException("좋아요 수는 0보다 작아질 수 없습니다.");
        }
        this.likesCount--;
    }
}
