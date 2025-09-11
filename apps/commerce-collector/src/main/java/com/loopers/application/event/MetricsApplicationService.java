package com.loopers.application.event;


import com.loopers.domain.repository.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsApplicationService {

    private final IdempotentProcessor idempotentProcessor;
    private final ProductMetricsRepository productMetricsRepository;

    private static final String CONSUMER_GROUP = "metrics-group";
    
    /**
     * 배치 메트릭 업데이트 - 집계된 데이터를 배치로 처리
     */
    @Transactional
    public void updateMetricsBatch(ProductMetricsAggregation aggregation) {
        log.info("배치 메트릭 업데이트 시작 - View: {}, Like: {}, Sales: {}", 
                aggregation.getViewCounts().size(),
                aggregation.getLikeUpdates().size(),
                aggregation.getSalesData().size());
        
        // View 배치 업데이트
        aggregation.getViewCounts().values().forEach(viewCount -> 
            productMetricsRepository.incrementViewCountBatch(
                viewCount.getProductId(), 
                viewCount.getCount(),
                viewCount.getLastViewedAt()
            ));
        
        // Like 배치 업데이트
        aggregation.getLikeUpdates().values().forEach(likeUpdate ->
            productMetricsRepository.updateLikeCountBatch(
                likeUpdate.getProductId(),
                likeUpdate.getFinalLikeCount(), 
                likeUpdate.getLastLikedAt()
            ));
        
        // Sales 배치 업데이트
        aggregation.getSalesData().values().forEach(salesData ->
            productMetricsRepository.incrementSalesCountBatch(
                salesData.getProductId(),
                salesData.getSalesCount(),
                salesData.getTotalAmount()
            ));
            
        log.info("배치 메트릭 업데이트 완료");
    }
    
    /**
     * 이벤트 전체를 처리 완료로 마크
     */
    public void markEventsAsProcessed(List<Map<String, Object>> eventDataList) {
        eventDataList.forEach(eventData -> {
            String eventId = (String) eventData.get("eventId");
            idempotentProcessor.markAsProcessed(eventId, CONSUMER_GROUP);
        });
        
        log.debug("이벤트 멱등성 마크 완료 - 처리된 이벤트 수: {}", eventDataList.size());
    }

}
