package com.loopers.interfaces.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingBatchScheduler {

    private static final Marker BATCH_FAILURE = MarkerFactory.getMarker("BATCH_FAILURE");

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    /**
     * 매일 자정 10분에 주간 랭킹 배치 실행 (최근 7일 기준)
     */
    @Scheduled(cron = "0 10 0 * * ?")
    public void runWeeklyRankingBatch() {
        try {
            log.info("주간 랭킹 배치 스케줄러 실행 시작");

            LocalDate today = LocalDate.now();
            LocalDate endDate = today.minusDays(1); // 어제까지
            LocalDate startDate = endDate.minusDays(6); // 7일 전부터

            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            int weekYear = endDate.get(weekFields.weekBasedYear());
            int weekNumber = endDate.get(weekFields.weekOfWeekBasedYear());

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startDate.toString())
                    .addString("endDate", endDate.toString())
                    .addString("targetDate", endDate.toString())
                    .addLong("maxRankSize", 100L)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(weeklyRankingJob, jobParameters);

            log.info("주간 랭킹 배치 스케줄러 실행 완료 - {}년 {}주차 (집계기간: {} ~ {})",
                    weekYear, weekNumber, startDate, endDate);

        } catch (Exception e) {
            log.error(BATCH_FAILURE, "주간 랭킹 배치 실행 실패\n" +
                    "배치명: weeklyRankingJob\n" +
                    "실행시간: {}\n" +
                    "오류내용: {}",
                    LocalDate.now(), e.getMessage(), e);
        }
    }

    /**
     * 매일 자정 15분에 월간 랭킹 배치 실행 (현재 월 1일부터 어제까지)
     */
    @Scheduled(cron = "0 15 0 * * ?")
    public void runMonthlyRankingBatch() {
        try {
            log.info("월간 랭킹 배치 스케줄러 실행 시작");

            LocalDate today = LocalDate.now();
            LocalDate endDate = today.minusDays(1); // 어제까지
            LocalDate startDate = endDate.withDayOfMonth(1); // 어제 기준 월의 1일부터

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("startDate", startDate.toString())
                    .addString("endDate", endDate.toString())
                    .addString("targetDate", endDate.toString())
                    .addLong("maxRankSize", 100L)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(monthlyRankingJob, jobParameters);

            log.info("월간 랭킹 배치 스케줄러 실행 완료 - {}년 {}월 (집계기간: {} ~ {})",
                    endDate.getYear(), endDate.getMonthValue(), startDate, endDate);

        } catch (Exception e) {
            log.error(BATCH_FAILURE, "월간 랭킹 배치 실행 실패\n" +
                    "배치명: monthlyRankingJob\n" +
                    "실행시간: {}\n" +
                    "오류내용: {}",
                    LocalDate.now(), e.getMessage(), e);
        }
    }
}
