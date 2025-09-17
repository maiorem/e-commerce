package com.loopers.domain.ranking;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mv_product_rank_weekly",
       uniqueConstraints = @UniqueConstraint(columnNames = {"week_year", "week_number", "product_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyProductRanking extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "week_year", nullable = false)
    private Integer weekYear;

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;

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

    public static WeeklyProductRanking create(
            Long productId,
            Integer weekYear,
            Integer weekNumber,
            Integer rankPosition,
            Double totalScore,
            Long viewCount,
            Long likeCount,
            Long salesCount,
            Long totalSalesAmount) {

        WeeklyProductRanking ranking = new WeeklyProductRanking();
        ranking.productId = productId;
        ranking.weekYear = weekYear;
        ranking.weekNumber = weekNumber;
        ranking.rankPosition = rankPosition;
        ranking.totalScore = totalScore;
        ranking.viewCount = viewCount;
        ranking.likeCount = likeCount;
        ranking.salesCount = salesCount;
        ranking.totalSalesAmount = totalSalesAmount;

        return ranking;
    }
}