package com.loopers.infrastructure.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyRankingJpaQueryRepository extends JpaRepository<WeeklyRankingQueryEntity, Long> {
    
    @Query("SELECT w FROM WeeklyRankingQueryEntity w WHERE w.weekYear = :weekYear AND w.weekNumber = :weekNumber ORDER BY w.rankPosition")
    Page<WeeklyRankingQueryEntity> findByWeekYearAndWeekNumber(
            @Param("weekYear") Integer weekYear, 
            @Param("weekNumber") Integer weekNumber, 
            Pageable pageable);
}
