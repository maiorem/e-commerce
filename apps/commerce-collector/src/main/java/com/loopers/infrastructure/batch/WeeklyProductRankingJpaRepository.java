package com.loopers.infrastructure.batch;

import com.loopers.domain.ranking.WeeklyProductRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyProductRankingJpaRepository extends JpaRepository<WeeklyProductRanking, Long> {
    
    @Modifying
    @Query("DELETE FROM WeeklyProductRanking w WHERE w.weekYear = :weekYear AND w.weekNumber = :weekNumber")
    void deleteByWeekYearAndWeekNumber(@Param("weekYear") Integer weekYear, @Param("weekNumber") Integer weekNumber);
    
    List<WeeklyProductRanking> findTop100ByWeekYearAndWeekNumberOrderByRankPosition(Integer weekYear, Integer weekNumber);
}