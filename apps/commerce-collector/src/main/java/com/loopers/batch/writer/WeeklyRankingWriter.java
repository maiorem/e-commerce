package com.loopers.batch.writer;

import com.loopers.domain.ranking.ScoredProductMetrics;
import com.loopers.domain.ranking.WeeklyProductRanking;
import com.loopers.domain.repository.WeeklyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class WeeklyRankingWriter implements ItemWriter<ScoredProductMetrics> {

    private final WeeklyProductRankingRepository weeklyProductRankingRepository;
    private final RankingGenerator rankingGenerator;
    
    @Value("#{jobParameters['targetDate']}")
    private String targetDateParam;
    
    @Value("#{jobParameters['maxRankSize'] ?: 100}")
    private int maxRankSize;
    
    private static final List<ScoredProductMetrics> allCollectedMetrics = Collections.synchronizedList(new ArrayList<>());
    private static boolean isFirstChunk = true;

    @Override
    public void write(Chunk<? extends ScoredProductMetrics> items) throws Exception {
        LocalDate targetDate = LocalDate.parse(targetDateParam);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekYear = targetDate.get(weekFields.weekBasedYear());
        int weekNumber = targetDate.get(weekFields.weekOfWeekBasedYear());
        
        // 첫 번째 청크에서 초기화
        if (isFirstChunk) {
            log.info("주간 랭킹 Writer 초기화 - {}년 {}주차", weekYear, weekNumber);
            allCollectedMetrics.clear();
            weeklyProductRankingRepository.deleteByWeekYearAndWeekNumber(weekYear, weekNumber);
            isFirstChunk = false;
        }
        
        // 현재 청크 데이터 수집
        allCollectedMetrics.addAll(items.getItems());
        log.debug("청크 처리 완료 - 현재까지 수집된 데이터: {}개", allCollectedMetrics.size());
        
        // 전체 랭킹 생성 및 저장
        generateAndSaveRanking(weekYear, weekNumber);
    }
    
    public void generateAndSaveRanking(int weekYear, int weekNumber) {
        if (allCollectedMetrics.isEmpty()) {
            return;
        }
        
        // 기존 데이터 삭제
        weeklyProductRankingRepository.deleteByWeekYearAndWeekNumber(weekYear, weekNumber);
        
        // 랭킹 생성
        List<WeeklyProductRanking> rankings = rankingGenerator.generateWeeklyRankings(
                allCollectedMetrics, weekYear, weekNumber, maxRankSize);
        
        // 랭킹 저장
        weeklyProductRankingRepository.saveAllRankings(rankings);
        
        log.info("주간 랭킹 저장 완료 - {}개 랭킹", rankings.size());
        
        // Job 완료를 위한 정리
        isFirstChunk = true;
    }
}
