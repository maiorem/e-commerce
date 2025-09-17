package com.loopers.batch.writer;

import com.loopers.domain.ranking.MonthlyProductRanking;
import com.loopers.domain.ranking.ScoredProductMetrics;
import com.loopers.domain.repository.MonthlyProductRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class MonthlyRankingWriter implements ItemWriter<ScoredProductMetrics> {

    private final MonthlyProductRankingRepository monthlyProductRankingRepository;
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
        int year = targetDate.getYear();
        int month = targetDate.getMonthValue();

        if (isFirstChunk) {
            log.info("월간 랭킹 Writer 초기화 - {}년 {}월", year, month);
            allCollectedMetrics.clear();
            monthlyProductRankingRepository.deleteByYearAndMonth(year, month);
            isFirstChunk = false;
        }

        allCollectedMetrics.addAll(items.getItems());
        log.debug("청크 처리 완료 - 현재까지 수집된 데이터: {}개", allCollectedMetrics.size());

        generateAndSaveRanking(year, month);
    }

    public void generateAndSaveRanking(int year, int month) {
        if (allCollectedMetrics.isEmpty()) {
            return;
        }

        monthlyProductRankingRepository.deleteByYearAndMonth(year, month);

        List<MonthlyProductRanking> rankings = rankingGenerator.generateMonthlyRankings(
                allCollectedMetrics, year, month, maxRankSize);

        monthlyProductRankingRepository.saveAllRankings(rankings);

        log.info("월간 랭킹 저장 완료 - {}개 랭킹", rankings.size());

        isFirstChunk = true;
    }
}