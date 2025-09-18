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

    @Column(name = "total_sales_amount", nullable = false)
    private Long totalSalesAmount = 0L;

    @Column(name = "last_viewed_at")
    private ZonedDateTime lastViewedAt;

    @Column(name = "last_liked_at")
    private ZonedDateTime lastLikedAt;

    @Column(name = "aggregate_date", nullable = false)
    private ZonedDateTime aggregateDate;

    public static ProductMetrics of(Long productId) {
        ProductMetrics metrics = new ProductMetrics();
        metrics.productId = productId;
        metrics.aggregateDate = ZonedDateTime.now(); // 생성 시점을 집계 기준일로 설정
        return metrics;
    }

    public void incrementViewCount(ZonedDateTime viewedAt) {
        this.viewCount++;
        this.lastViewedAt = viewedAt;
        this.aggregateDate = ZonedDateTime.now(); // 메트릭 업데이트 시점으로 집계 기준일 갱신
    }

    public void updateLikeCount(Long newLikeCount, ZonedDateTime likedAt) {
        this.likeCount = newLikeCount;
        this.lastLikedAt = likedAt;
        this.aggregateDate = ZonedDateTime.now(); // 메트릭 업데이트 시점으로 집계 기준일 갱신
    }

    public void incrementSalesCount(Long amount) {
        this.salesCount++;
        this.totalSalesAmount += amount;
        this.aggregateDate = ZonedDateTime.now(); // 메트릭 업데이트 시점으로 집계 기준일 갱신
    }

}
