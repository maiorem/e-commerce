package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.MonthlyProductRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyProductRankingJpaRepository extends JpaRepository<MonthlyProductRanking, Long> {
    
    @Modifying
    @Query("DELETE FROM MonthlyProductRanking m WHERE m.year = :year AND m.month = :month")
    void deleteByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);
}
