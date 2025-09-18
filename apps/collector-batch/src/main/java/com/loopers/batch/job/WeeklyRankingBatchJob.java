package com.loopers.batch.job;

import com.loopers.batch.processor.WeeklyRankingProcessor;
import com.loopers.batch.reader.ProductMetricsReader;
import com.loopers.batch.tasklet.WeeklyRankingPersistenceTasklet;
import com.loopers.config.BatchConfigProperties;
import com.loopers.domain.entity.ProductMetrics;
import com.loopers.monitoring.BatchPerformanceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ListItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WeeklyRankingBatchJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchConfigProperties batchConfigProperties;

    private final ProductMetricsReader productMetricsReader;
    private final WeeklyRankingProcessor weeklyRankingProcessor;
    private final WeeklyRankingPersistenceTasklet weeklyRankingPersistenceTasklet;
    private final BatchPerformanceMonitor performanceMonitor;

    @Bean
    public Job weeklyRankingJob() {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(weeklyRankingDataProcessStep())
                .next(weeklyRankingPersistenceStep())
                .build();
    }

    @Bean
    public Step weeklyRankingDataProcessStep() {
        return new StepBuilder("weeklyRankingDataProcessStep", jobRepository)
                .<ProductMetrics, ProductMetrics>chunk(batchConfigProperties.getChunkSize(), transactionManager)
                .reader(productMetricsReader)
                .processor(weeklyRankingProcessor)
                .writer(new ListItemWriter<>()) // null을 반환하므로 빈 Writer 사용
                .listener(performanceMonitor)
                .faultTolerant()
                .skipLimit(batchConfigProperties.getSkipLimit())
                .skip(Exception.class)
                .build();
    }

    @Bean
    public Step weeklyRankingPersistenceStep() {
        return new StepBuilder("weeklyRankingPersistenceStep", jobRepository)
                .tasklet(weeklyRankingPersistenceTasklet, transactionManager)
                .build();
    }
}