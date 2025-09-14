package com.loopers.batch.job;

import com.loopers.batch.processor.MonthlyRankingProcessor;
import com.loopers.batch.reader.ProductMetricsReader;
import com.loopers.batch.writer.MonthlyRankingWriter;
import com.loopers.domain.entity.ProductMetrics;
import com.loopers.domain.ranking.ScoredProductMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MonthlyRankingBatchJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    private final ProductMetricsReader productMetricsReader;
    private final MonthlyRankingProcessor monthlyRankingProcessor;
    private final MonthlyRankingWriter monthlyRankingWriter;

    @Bean
    public Job monthlyRankingJob() {
        return new JobBuilder("monthlyRankingJob", jobRepository)
                .start(monthlyRankingStep())
                .build();
    }

    @Bean
    public Step monthlyRankingStep() {
        return new StepBuilder("monthlyRankingStep", jobRepository)
                .<ProductMetrics, ScoredProductMetrics>chunk(1000, transactionManager)
                .reader(productMetricsReader)
                .processor(monthlyRankingProcessor)
                .writer(monthlyRankingWriter)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }
}