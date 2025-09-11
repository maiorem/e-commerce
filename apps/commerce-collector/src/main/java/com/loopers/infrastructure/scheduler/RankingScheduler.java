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
     * 매일 자정에 전날 랭킹을 DB에서 Redis로 동기화
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncDailyRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        try {
            log.info("일일 랭킹 동기화 스케줄 실행 - Date: {}", yesterday);
            rankingCacheService.syncRankingFromDatabase(yesterday);
            log.info("일일 랭킹 동기화 스케줄 완료 - Date: {}", yesterday);
        } catch (Exception e) {
            log.error("일일 랭킹 동기화 스케줄 실패 - Date: {}, Error: {}", yesterday, e.getMessage(), e);
        }
    }

    /**
     * 매시간 정각에 오늘 랭킹을 DB에서 Redis로 동기화
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncHourlyRanking() {
        LocalDate today = LocalDate.now();
        
        try {
            log.info("시간별 랭킹 동기화 스케줄 실행 - Date: {}", today);
            rankingCacheService.syncRankingFromDatabase(today);
            log.info("시간별 랭킹 동기화 스케줄 완료 - Date: {}", today);
        } catch (Exception e) {
            log.error("시간별 랭킹 동기화 스케줄 실패 - Date: {}, Error: {}", today, e.getMessage(), e);
        }
    }

    /**
     * 매 5분마다 오늘 랭킹을 DB에서 Redis로 동기화
     */
    @Scheduled(fixedDelay = 300000) // 5분
    public void syncRealtimeRanking() {
        LocalDate today = LocalDate.now();
        
        try {
            log.debug("실시간 랭킹 동기화 스케줄 실행 - Date: {}", today);
            rankingCacheService.syncRankingFromDatabase(today);
            log.debug("실시간 랭킹 동기화 스케줄 완료 - Date: {}", today);
        } catch (Exception e) {
            log.error("실시간 랭킹 동기화 스케줄 실패 - Date: {}, Error: {}", today, e.getMessage(), e);
        }
    }
}
