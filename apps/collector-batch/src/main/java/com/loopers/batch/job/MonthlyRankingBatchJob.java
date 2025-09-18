package com.loopers.batch.job;

import com.loopers.batch.processor.MonthlyRankingProcessor;
import com.loopers.batch.reader.ProductMetricsReader;
import com.loopers.batch.tasklet.MonthlyRankingPersistenceTasklet;
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
public class MonthlyRankingBatchJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchConfigProperties batchConfigProperties;

    private final ProductMetricsReader productMetricsReader;
    private final MonthlyRankingProcessor monthlyRankingProcessor;
    private final MonthlyRankingPersistenceTasklet monthlyRankingPersistenceTasklet;
    private final BatchPerformanceMonitor performanceMonitor;

    @Bean
    public Job monthlyRankingJob() {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .start(monthlyRankingDataProcessStep())
                .next(monthlyRankingPersistenceStep())
                .build();
    }

    @Bean
    public Step monthlyRankingDataProcessStep() {
        return new StepBuilder("monthlyRankingDataProcessStep", jobRepository)
                .<ProductMetrics, ProductMetrics>chunk(batchConfigProperties.getChunkSize(), transactionManager)
                .reader(productMetricsReader)
                .processor(monthlyRankingProcessor)
                .writer(new ListItemWriter<>()) // null을 반환하므로 빈 Writer 사용
                .listener(performanceMonitor)
                .faultTolerant()
                .skipLimit(batchConfigProperties.getSkipLimit())
                .skip(Exception.class)
                .build();
    }

    @Bean
    public Step monthlyRankingPersistenceStep() {
        return new StepBuilder("monthlyRankingPersistenceStep", jobRepository)
                .tasklet(monthlyRankingPersistenceTasklet, transactionManager)
                .build();
    }
}