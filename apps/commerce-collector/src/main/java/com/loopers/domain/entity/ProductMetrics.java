package com.loopers.domain.entity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(name = "product_metrics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMetrics extends BaseEntity {

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount = 0L;

    @Column(name = "last_viewed_at")
    private ZonedDateTime lastViewedAt;

    @Column(name = "last_liked_at")
    private ZonedDateTime lastLikedAt;

    public static ProductMetrics of(Long productId) {
        ProductMetrics metrics = new ProductMetrics();
        metrics.productId = productId;
        return metrics;
    }

    public void incrementViewCount(ZonedDateTime viewedAt) {
        this.viewCount++;
        this.lastViewedAt = viewedAt;
    }

    public void updateLikeCount(Long newLikeCount, ZonedDateTime likedAt) {
        this.likeCount = newLikeCount;
        this.lastLikedAt = likedAt;
    }

    public void incrementSalesCount() {
        this.salesCount++;
    }

}
