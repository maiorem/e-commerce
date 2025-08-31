package com.loopers.domain.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetrics {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount = 0L;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "last_liked_at")
    private LocalDateTime lastLikedAt;

    @Column(name = "last_sold_at")
    private LocalDateTime lastSoldAt;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    public ProductMetrics(Long productId) {
        this.productId = productId;
        this.updatedAt = LocalDateTime.now();
    }

    public static ProductMetrics create(Long productId) {
        return new ProductMetrics(productId);
    }

    public void increaseLikeCount(int delta) {
        this.likeCount += delta;
        this.lastLikedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseSalesCount(int quantity) {
        this.salesCount += quantity;
        this.lastSoldAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void increaseViewCount() {
        this.viewCount += 1;
        this.lastViewedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
