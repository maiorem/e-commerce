package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.MonthlyProductRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyProductRankingJpaRepository extends JpaRepository<MonthlyProductRanking, Long> {

    @Modifying
    @Query("DELETE FROM MonthlyProductRanking m WHERE m.year = :year AND m.month = :month")
    void deleteByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    List<MonthlyProductRanking> findByYearAndMonth(Integer year, Integer month);

    @Query("SELECT m FROM MonthlyProductRanking m WHERE m.year = :year AND m.month = :month ORDER BY m.rankPosition LIMIT :limit")
    List<MonthlyProductRanking> findTopRankingsByYearAndMonth(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("limit") int limit);
}