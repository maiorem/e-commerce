package com.loopers.batch.job;

import com.loopers.batch.processor.WeeklyRankingProcessor;
import com.loopers.batch.reader.ProductMetricsReader;
import com.loopers.batch.writer.WeeklyRankingWriter;
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
public class WeeklyRankingBatchJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    private final ProductMetricsReader productMetricsReader;
    private final WeeklyRankingProcessor weeklyRankingProcessor;
    private final WeeklyRankingWriter weeklyRankingWriter;

    @Bean
    public Job weeklyRankingJob() {
        return new JobBuilder("weeklyRankingJob", jobRepository)
                .start(weeklyRankingStep())
                .build();
    }

    @Bean
    public Step weeklyRankingStep() {
        return new StepBuilder("weeklyRankingStep", jobRepository)
                .<ProductMetrics, ScoredProductMetrics>chunk(1000, transactionManager)
                .reader(productMetricsReader)
                .processor(weeklyRankingProcessor)
                .writer(weeklyRankingWriter)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }
}
