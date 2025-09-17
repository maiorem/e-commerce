package com.loopers.infrastructure.ranking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyRankingJpaQueryRepository extends JpaRepository<MonthlyRankingQueryEntity, Long> {
    
    @Query("SELECT m FROM MonthlyRankingQueryEntity m WHERE m.year = :year AND m.month = :month ORDER BY m.rankPosition")
    Page<MonthlyRankingQueryEntity> findByYearAndMonth(
            @Param("year") Integer year, 
            @Param("month") Integer month, 
            Pageable pageable);

}
