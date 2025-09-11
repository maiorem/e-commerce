package com.loopers.infrastructure.scheduler;

import com.loopers.application.event.RankingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingCacheService rankingCacheService;

    /**
     * 매일 자정 1분에 전날의 최종 랭킹 점수를 Redis에 업데이트.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void updateAndRefreshRanking() {
        log.info("일간 랭킹 업데이트 스케줄러 실행");
        rankingCacheService.updateDailyRanking(LocalDate.now().minusDays(1));
        log.info("일간 랭킹 업데이트 스케줄러 완료");
    }

    /**
     * 매일 23시 50분에 전날의 랭킹 데이터를 다음날 랭킹으로 이월. (콜드 스타트 대비)
     */
    @Scheduled(cron = "0 50 23 * * ?")
    public void carryOverRankingScores() {
        log.info("랭킹 점수 이월 스케줄러 실행");
        rankingCacheService.carryOverRankingScores();
        log.info("랭킹 점수 이월 스케줄러 완료");
    }
}
