package com.loopers.interfaces.scheduler;

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
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    /**
     * 매주 월요일 오전 2시에 주간 랭킹 배치
     */
    @Scheduled(cron = "0 0 2 * * MON")
    public void executeWeeklyRankingBatch() {
        try {
            log.info("주간 랭킹 배치 스케줄러 실행");

            LocalDate targetDate = LocalDate.now().minusDays(1);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", targetDate.minusDays(6).toString())
                    .addString("endDate", targetDate.toString())
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
     * 매월 1일 오전 3시에 월간 랭킹 배치
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void executeMonthlyRankingBatch() {
        try {
            log.info("월간 랭킹 배치 스케줄러 실행");

            LocalDate targetDate = LocalDate.now().minusDays(1);
            LocalDate startOfMonth = targetDate.withDayOfMonth(1);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startOfMonth.toString())
                    .addString("endDate", targetDate.toString())
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