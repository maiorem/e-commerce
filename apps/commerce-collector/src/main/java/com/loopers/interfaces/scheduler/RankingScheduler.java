package com.loopers.interfaces.scheduler;

import com.loopers.application.event.RankingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingCacheService rankingCacheService;
    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

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

    /**
     * 매주 월요일 오전 2시에 주간 랭킹
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void executeWeeklyRankingBatch() {
        try {
            log.info("주간 랭킹 배치 스케줄러 실행");
            
            LocalDate targetDate = LocalDate.now().minusDays(1);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(weeklyRankingJob, jobParameters);
            log.info("주간 랭킹 배치 스케줄러 완료");
        } catch (Exception e) {
            log.error("주간 랭킹 배치 실행 실패", e);
        }
    }

    /**
     * 매월 1일 오전 3시에 월간 랭킹
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void executeMonthlyRankingBatch() {
        try {
            log.info("월간 랭킹 배치 스케줄러 실행");
            
            LocalDate targetDate = LocalDate.now().minusDays(1);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("targetDate", targetDate.toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(monthlyRankingJob, jobParameters);
            log.info("월간 랭킹 배치 스케줄러 완료");
        } catch (Exception e) {
            log.error("월간 랭킹 배치 실행 실패", e);
        }
    }
}
