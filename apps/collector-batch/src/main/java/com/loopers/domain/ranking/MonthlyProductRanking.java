package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mv_product_rank_monthly",
       uniqueConstraints = @UniqueConstraint(columnNames = {"year", "month", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyProductRanking extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;

    @Column(name = "total_score", nullable = false, precision = 10, scale = 2)
    private Double totalScore;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    private Long likeCount = 0L;

    @Column(name = "sales_count", nullable = false)
    private Long salesCount = 0L;

    @Column(name = "total_sales_amount", nullable = false)
    private Long totalSalesAmount = 0L;

    public static MonthlyProductRanking create(
            Long productId,
            Integer year,
            Integer month,
            Integer rankPosition,
            Double totalScore,
            Long viewCount,
            Long likeCount,
            Long salesCount,
            Long totalSalesAmount) {

        MonthlyProductRanking ranking = new MonthlyProductRanking();
        ranking.productId = productId;
        ranking.year = year;
        ranking.month = month;
        ranking.rankPosition = rankPosition;
        ranking.totalScore = totalScore;
        ranking.viewCount = viewCount;
        ranking.likeCount = likeCount;
        ranking.salesCount = salesCount;
        ranking.totalSalesAmount = totalSalesAmount;

        return ranking;
    }
}