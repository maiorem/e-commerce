package com.loopers.batch.reader;

import com.loopers.domain.entity.ProductMetrics;
import com.loopers.infrastructure.event.ProductMetricsJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class ProductMetricsReader extends RepositoryItemReader<ProductMetrics> {

    private final ProductMetricsJpaRepository productMetricsJpaRepository;
    
    @Value("#{jobParameters['startDate']}")
    private String startDateParam;
    
    @Value("#{jobParameters['endDate']}")  
    private String endDateParam;

    @PostConstruct
    public void initialize() {
        LocalDate startDate = LocalDate.parse(startDateParam);
        LocalDate endDate = LocalDate.parse(endDateParam);
        
        ZonedDateTime startDateTime = startDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault());
        ZonedDateTime endDateTime = endDate.plusDays(1).atStartOfDay().atZone(java.time.ZoneId.systemDefault());
        
        log.info("ProductMetrics Reader 초기화 - 기간: {} ~ {}", startDateTime, endDateTime);
        
        setRepository(productMetricsJpaRepository);
        setMethodName("findByUpdatedAtBetween");
        
        setArguments(java.util.Arrays.asList(startDateTime, endDateTime));
        
        setPageSize(1000);
        
        // 정렬 설정
        Map<String, Sort.Direction> sorts = new HashMap<>();
        sorts.put("id", Sort.Direction.ASC);
        setSort(sorts);
        
        // 저장하지 않고 읽기만
        setSaveState(false);
    }
}
